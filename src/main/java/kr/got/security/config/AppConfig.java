package kr.got.security.config;

import kr.got.security.repository.AccessIpRepository;
import kr.got.security.repository.ResourcesRepository;
import kr.got.security.security.service.SecurityResourceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public SecurityResourceService securityResourceService(ResourcesRepository resourcesRepository,
                                                           AccessIpRepository accessIpRepository) {
        return new SecurityResourceService(resourcesRepository, accessIpRepository);
    }
}
