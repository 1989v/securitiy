package kr.got.security.service.impl;

import kr.got.security.repository.RoleHierarchyRepository;
import kr.got.security.service.RoleHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoleHierarchyServiceImpl implements RoleHierarchyService {
    private final RoleHierarchyRepository roleHierarchyRepository;

    @Override
    public String findAllHierarchy() {
        return roleHierarchyRepository.findAll().stream()
                .filter(roleHierarchy -> roleHierarchy.getParentName() != null)
                .map(roleHierarchy -> roleHierarchy
                        .getParentName().getChildName()
                        .concat(" > ")
                        .concat(roleHierarchy.getChildName())
                        .concat("\n"))
                .collect(Collectors.joining());
    }
}
