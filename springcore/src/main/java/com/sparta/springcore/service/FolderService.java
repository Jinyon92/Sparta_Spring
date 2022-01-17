package com.sparta.springcore.service;

import com.sparta.springcore.model.Folder;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.User;
import com.sparta.springcore.repository.FolderRepository;
import com.sparta.springcore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FolderService {
    private final FolderRepository folderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository, ProductRepository productRepository){
        this.folderRepository = folderRepository;
        this.productRepository = productRepository;
    }

    // 로그인한 회원에 폴더들 등록
    @Transactional
    public List<Folder> addFolders(List<String> folderNames, User user) {
        // 1) 입력으로 들어온 폴더 이름을 기준으로, 회원이 이미 생성한 폴더들을 조회합니다.
        List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);

        List<Folder> savedFolderList = new ArrayList<>();
        for (String folderName : folderNames) {
            // 2) 이미 생성한 폴더가 아닌 경우만 폴더 생성
            if (isExistFolderName(folderName, existFolderList)) {
                // Exception 발생!
                throw new IllegalArgumentException("중복된 폴더명을 제거해 주세요! 폴더명: " + folderName);
            } else {
                Folder folder = new Folder(folderName, user);
                // 폴더명 저장
                folder = folderRepository.save(folder);
                savedFolderList.add(folder);
            }
        }

        return savedFolderList;
    }

    // 로그인한 회원이 등록된 모든 폴더 조회
    public List<Folder> getFolders(User user) {
        return folderRepository.findAllByUser(user);
    }

    // 회원 ID가 소유한 폴더에 저장되어 있는 상품들 조회
    public Page<Product> getProductsInFolder(int page, int size, String sortBy,
                                             boolean isAsc, User user, Long folderId) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Long loginUserId = user.getId();

        return productRepository.findAllByUserIdAndFolderList_Id(loginUserId, folderId, pageable);
    }

    // 기존 폴더 유무 확인
    private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
        for(Folder existFolder : existFolderList){
            if(folderName.equals(existFolder.getName())) {
                return true;
            }
        }

        return false;
    }
}
