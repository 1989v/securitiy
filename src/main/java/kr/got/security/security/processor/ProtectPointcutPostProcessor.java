package kr.got.security.security.processor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.MapBasedMethodSecurityMetadataSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class ProtectPointcutPostProcessor implements BeanPostProcessor {
    private final Map<String, List<ConfigAttribute>> pointcutMap = new LinkedHashMap<>();
    private final MapBasedMethodSecurityMetadataSource mapBasedMethodSecurityMetadataSource;
    private final Set<PointcutExpression> pointCutExpressions = new LinkedHashSet<>();
    private final PointcutParser parser;
    private final Set<String> processedBeans = new HashSet<>();

    public ProtectPointcutPostProcessor(MapBasedMethodSecurityMetadataSource mapBasedMethodSecurityMetadataSource) {
        Assert.notNull(mapBasedMethodSecurityMetadataSource,
                "MapBasedMethodSecurityMetadataSource to populate is required");
        this.mapBasedMethodSecurityMetadataSource = mapBasedMethodSecurityMetadataSource;

        Set<PointcutPrimitive> supportedPrimitives = new HashSet<>(3);
        supportedPrimitives.add(PointcutPrimitive.EXECUTION);
        supportedPrimitives.add(PointcutPrimitive.ARGS);
        supportedPrimitives.add(PointcutPrimitive.REFERENCE);
        this.parser = PointcutParser
                .getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(
                        supportedPrimitives);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (this.processedBeans.contains(beanName)) {
            return bean;
        }
        synchronized (this.processedBeans) {
            if (this.processedBeans.contains(beanName)) {
                return bean;
            }

            Method[] methods = getBeanMethods(bean);
            for (Method method : methods) {
                for (PointcutExpression expression : this.pointCutExpressions) {
                    if (attemptMatch(bean.getClass(), method, expression, beanName)) {
                        break;
                    }
                }
            }
            this.processedBeans.add(beanName);
        }
        return bean;
    }

    private Method[] getBeanMethods(Object bean) {
        try {
            return bean.getClass().getMethods();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    /**
     * 설정클래스에서 람다 형식으로 선언된 빈이 존재할 경우 에러가 발생하여 스프링 빈과 동일한 클래스를 생성하여 약간 수정함
     * 아직 AspectJ 라이브러리에서 Fix 하지 못한 것으로 판단되지만 다른 원인이 존재하는지 계속 살펴보도록 함
     */
    private boolean attemptMatch(Class<?> targetClass, Method method, PointcutExpression expression, String beanName) {
        boolean matches;

        try {
            matches = expression.matchesMethodExecution(method).alwaysMatches();

            if (matches) {
                List<ConfigAttribute> attr = this.pointcutMap.get(expression.getPointcutExpression());
                if (log.isDebugEnabled()) {
                    log.debug("AspectJ pointcut expression '" + expression.getPointcutExpression()
                            + "' matches target class '" + targetClass.getName() + "' (bean ID '" + beanName
                            + "') for method '" + method + "'; registering security configuration attribute '" + attr
                            + "'");
                }
                this.mapBasedMethodSecurityMetadataSource.addSecureMethod(targetClass, method, attr);
            }
        } catch (Exception e) {
            matches = false;
        }

        return matches;
    }

    public void setPointcutMap(Map<String, List<ConfigAttribute>> map) {
        Assert.notEmpty(map, "configAttributes cannot be empty");
        for (String expression : map.keySet()) {
            List<ConfigAttribute> value = map.get(expression);
            addPointcut(expression, value);
        }
    }

    private void addPointcut(String pointcutExpression, List<ConfigAttribute> definition) {
        Assert.hasText(pointcutExpression, "An AspectJ pointcut expression is required");
        Assert.notNull(definition, "A List of ConfigAttributes is required");
        pointcutExpression = replaceBooleanOperators(pointcutExpression);
        this.pointcutMap.put(pointcutExpression, definition);
        // Parse the presented AspectJ pointcut expression and add it to the cache
        this.pointCutExpressions.add(this.parser.parsePointcutExpression(pointcutExpression));
        if (log.isDebugEnabled()) {
            log.debug("AspectJ pointcut expression '" + pointcutExpression
                    + "' registered for security configuration attribute '" + definition + "'");
        }
    }

    private String replaceBooleanOperators(String pcExpr) {
        pcExpr = StringUtils.replace(pcExpr, " and ", " && ");
        pcExpr = StringUtils.replace(pcExpr, " or ", " || ");
        pcExpr = StringUtils.replace(pcExpr, " not ", " ! ");
        return pcExpr;
    }

}

