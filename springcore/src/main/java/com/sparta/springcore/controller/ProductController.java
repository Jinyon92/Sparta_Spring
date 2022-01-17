package com.sparta.springcore.controller;

import com.sparta.springcore.model.ApiUseTime;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.dto.ProductMypriceRequestDto;
import com.sparta.springcore.dto.ProductRequestDto;
import com.sparta.springcore.model.User;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.repository.ApiUseTimeRepository;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor // final로 선언된 멤버 변수를 자동으로 생성합니다.
@RestController // JSON으로 데이터를 주고받음을 선언합니다.
public class ProductController {

    private final ProductService productService;
    private final ApiUseTimeRepository apiUseTimeRepository;

    // 신규 상품 등록
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 측정 시작 시간
        long startTime = System.currentTimeMillis();

        try {
            Long userId = userDetails.getUser().getId();

            Product product = productService.createProduct(requestDto, userId);

            // 응답 보내기
            return product;
        } finally {
            // 측정 종료 시간
            long endTime = System.currentTimeMillis();
            // 수행 시간 = 종료 시간 - 시작 시간
            long runTime = endTime - startTime;

            // 로그인 회원 정보
            User user = userDetails.getUser();

            // API 사용시간 및 DB에 기록
            ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(user).orElse(null);
            if(apiUseTime == null){
                apiUseTime = new ApiUseTime(user, runTime);
            }else{
                apiUseTime.addUseTime(runTime);
            }

            apiUseTimeRepository.save(apiUseTime);
        }
    }

    // 설정 가격 변경
    @PutMapping("/api/products/{id}")
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) {
        Product product = productService.updateProduct(id, requestDto);

        // 응답 보내기 (업데이트된 상품 id)
        return product.getId();
    }

    // 등록된 전체 상품 목록 조회
    @GetMapping("/api/products")
    public Page<Product> getProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        page = page - 1;
        // 응답 보내기
        return productService.getProducts(userId, page, size, sortBy, isAsc);
    }

    // (관리자용) 등록된 모든 상품 목록 조회
    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @GetMapping("/api/admin/products")
    public Page<Product> getAllProducts(@RequestParam("page") int page,
                                        @RequestParam("size") int size,
                                        @RequestParam("sortBy") String sortBy,
                                        @RequestParam("isAsc") boolean isAsc) {
        page = page - 1;
        return productService.getAllProducts(page, size, sortBy, isAsc);
    }

    @PostMapping("api/products/{productId}/folder")
    public Long addFolder(@PathVariable Long productId,
                          @RequestParam Long folderId,
                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        Product product = productService.addFolder(productId, folderId, user);
        return product.getId();
    }
}
