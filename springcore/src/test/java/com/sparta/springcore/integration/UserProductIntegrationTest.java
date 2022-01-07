package com.sparta.springcore.integration;

import com.sparta.springcore.dto.ProductMypriceRequestDto;
import com.sparta.springcore.dto.ProductRequestDto;
import com.sparta.springcore.dto.SignupRequestDto;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.User;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.repository.UserRepository;
import com.sparta.springcore.service.ProductService;
import com.sparta.springcore.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserProductIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    Long userId = null;
    Product createdProduct = null;
    int updatedMyPrice = -1;

    @Test
    @Order(1)
    @DisplayName("회원 가입 전 관심상품 등록(실패)")
    void beforeSignupRegisterProductTest(){
        // given
        String title = "Apple <b>에어팟</b> 2세대 유선충전 모델 (MV7N2KH/A)";
        String imageUrl = "https://shopping-phinf.pstatic.net/main_1862208/18622086330.20200831140839.jpg";
        String linkUrl = "https://search.shopping.naver.com/gate.nhn?id=18622086330";
        int lPrice = 77000;
        ProductRequestDto requestDto = new ProductRequestDto(
                title,
                imageUrl,
                linkUrl,
                lPrice
        );

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(requestDto, userId);
        });

        // then
        assertEquals("회원 Id 가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    @Order(2)
    @DisplayName("회원 가입")
    void SignupTest(){
        // given
        String username = "홍길동";
        String password = "123";
        String email = "Hong@sparta.com";
        boolean admin = false;

        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setUsername(username);
        requestDto.setPassword(password);
        requestDto.setEmail(email);
        requestDto.setAdmin(false);

        // when
        User user = userService.registerUser(requestDto);

        // then
        assertNotNull(user.getId());
        assertEquals(user.getUsername(), username);
        assertTrue(passwordEncoder.matches(password, user.getPassword()));
        assertEquals(user.getEmail(), email);
        assertEquals(user.getRole(), UserRoleEnum.USER);

        userId = user.getId();
    }

    @Test
    @Order(3)
    @DisplayName("가입된 회원으로 관심상품 등록")
    void afterSignupRegisterProductTest(){
        // given
        String title = "Apple <b>에어팟</b> 2세대 유선충전 모델 (MV7N2KH/A)";
        String imageUrl = "https://shopping-phinf.pstatic.net/main_1862208/18622086330.20200831140839.jpg";
        String linkUrl = "https://search.shopping.naver.com/gate.nhn?id=18622086330";
        int lPrice = 77000;
        ProductRequestDto requestDto = new ProductRequestDto(
                title,
                imageUrl,
                linkUrl,
                lPrice
        );

        // when
        Product product = productService.createProduct(requestDto, userId);

        // then
        assertNotNull(product.getId());
        assertEquals(userId, product.getUserId());
        assertEquals(title, product.getTitle());
        assertEquals(imageUrl, product.getImage());
        assertEquals(linkUrl, product.getLink());
        assertEquals(lPrice, product.getLprice());
        assertEquals(0, product.getMyprice());
        createdProduct = product;
    }

    @Test
    @Order(4)
    @DisplayName("관심상품 업데이트")
    void updateMyPriceTest() {
        // given
        int myPrice = 7000;
        Long productId = createdProduct.getId();
        ProductMypriceRequestDto requestDto = new ProductMypriceRequestDto(myPrice);

        // when
        Product updateProduct = productService.updateProduct(productId, requestDto);

        // then
        assertNotNull(updateProduct.getId());
        assertEquals(createdProduct.getUserId(), updateProduct.getUserId());
        assertEquals(createdProduct.getTitle(), updateProduct.getTitle());
        assertEquals(createdProduct.getImage(), updateProduct.getImage());
        assertEquals(createdProduct.getLink(), updateProduct.getLink());
        assertEquals(createdProduct.getLprice(), updateProduct.getLprice());
        assertEquals(myPrice, updateProduct.getMyprice());

        updatedMyPrice = myPrice;
    }

    @Test
    @Order(5)
    @DisplayName("관심상품 조회")
    void findProductTest() {
        // given
        List<Product> productList = productService.getAllProducts();

        // when
        // 1. 전체 상품에서 테스트에 의해 생성된 상품 찾아오기 (상품의 id로 찾음)
        Long createdProductId = this.createdProduct.getId();
        Product foundProduct = productList.stream()
                .filter(product -> product.getId().equals(createdProductId))
                .findFirst()
                .orElse(null);

        // then
        // 2. Order(1) 테스트에 의해 생성된 상품과 일치하는지 검증
        assertNotNull(foundProduct);
        assertEquals(userId, foundProduct.getUserId());
        assertEquals(this.createdProduct.getId(), foundProduct.getId());
        assertEquals(this.createdProduct.getTitle(), foundProduct.getTitle());
        assertEquals(this.createdProduct.getImage(), foundProduct.getImage());
        assertEquals(this.createdProduct.getLink(), foundProduct.getLink());
        assertEquals(this.createdProduct.getLprice(), foundProduct.getLprice());
        // 3. Order(2) 테스트에 의해 myPrice 가격이 정상적으로 업데이트되었는지 검증
        assertEquals(this.updatedMyPrice, foundProduct.getMyprice());
    }
}
