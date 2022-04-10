package kr.got.security.service.impl;

import kr.got.security.domain.entity.Resources;
import kr.got.security.repository.ResourcesRepository;
import kr.got.security.service.ResourcesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ResourcesServiceImpl implements ResourcesService {

    private final ResourcesRepository ResourcesRepository;


    public Resources getResources(long id) {
        return ResourcesRepository.findById(id).orElse(new Resources());
    }

    public List<Resources> getResources() {
        return ResourcesRepository.findAll(Sort.by(Sort.Order.asc("orderNum")));
    }

    public void createResources(Resources resources){
        ResourcesRepository.save(resources);
    }

    public void deleteResources(long id) {
        ResourcesRepository.deleteById(id);
    }
}
