package kr.got.security.security.voter;

import kr.got.security.security.service.SecurityResourceService;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.Collection;

public class IpAddressVoter implements AccessDecisionVoter<Object> {
    private final SecurityResourceService securityResourceService;

    public IpAddressVoter(SecurityResourceService securityResourceService) {
        this.securityResourceService = securityResourceService;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        String remoteAddress = details.getRemoteAddress();

        return securityResourceService.getAccessIpList().stream()
                .filter(accessIp -> accessIp.equals(remoteAddress))
                .findAny()
                .map(accessIp -> ACCESS_ABSTAIN)
                .orElseThrow(() -> new AccessDeniedException("Invalid IpAddress"));
    }
}
