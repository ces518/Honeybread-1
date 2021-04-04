package com.whatsub.honeybread.mgmtadmin.domain.category;

import com.whatsub.honeybread.core.domain.category.Category;
import com.whatsub.honeybread.core.domain.category.CategoryRepository;
import com.whatsub.honeybread.core.infra.errors.ErrorCode;
import com.whatsub.honeybread.core.infra.exception.HoneyBreadException;
import com.whatsub.honeybread.mgmtadmin.domain.category.dto.CategoryRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestConstructor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(classes = CategoryService.class)
@RequiredArgsConstructor
class CategoryServiceTest {
    final CategoryService service;

    @MockBean
    CategoryRepository repository;

    @Mock
    Category category;

    @Test
    void 중복되는_카테고리가_없다면_등록_성공() {
        // given
        중복되는_카테고리가_없다();
        given(repository.save(any(Category.class))).willReturn(category);

        // when
        카테고리_등록();

        // then
        중복_카테고리_확인이_수행되어야_한다();
        then(repository).should().save(any(Category.class));
    }

    @Test
    void 중복되는_카테고리가_있다면_예외_발생() {
        // given
        중복되는_카테고리가_있다();

        // when
        HoneyBreadException ex = assertThrows(HoneyBreadException.class, this::카테고리_등록);

        // then
        중복_카테고리_확인이_수행되어야_한다();
        중복_카테고리_에러코드_확인(ex);
    }

    @Test
    void 수정요청시_중복되는_카테고리가_없다면_수정_성공() {
        // given
        카테고리_조회에_성공한다();
        중복되는_카테고리가_없다();

        // when
        카테고리_수정();

        // then
        카테고리_조회가_수행되어야_한다();
        중복_카테고리_확인이_수행되어야_한다();
        then(category).should().update(any(Category.class));
    }

    @Test
    void 수정요청시_중복되는_카테고리가_있다면_예외_발생() {
        // given
        카테고리_조회에_성공한다();
        중복되는_카테고리가_있다();

        // when
        HoneyBreadException ex = assertThrows(HoneyBreadException.class, this::카테고리_수정);

        // then
        카테고리_조회가_수행되어야_한다();
        중복_카테고리_확인이_수행되어야_한다();
        중복_카테고리_에러코드_확인(ex);
    }

    @Test
    void 수정요청시_해당카테고리가_없다면_예외_발생() {
        // given
        카테고리_조회에_실패한다();

        // when
        HoneyBreadException ex = assertThrows(HoneyBreadException.class, this::카테고리_수정);

        // then
        카테고리_조회가_수행되어야_한다();
        카테고리_찾을수_없음_에러코드_확인(ex);
    }

    @Test
    void 삭제요청시_해당카테고리가_있다면_삭제_성공() {
        // given
        카테고리_조회에_성공한다();

        // when
        카테고리_삭제();

        // then
        카테고리_조회가_수행되어야_한다();
        then(repository).should().delete(any(Category.class));
    }

    @Test
    void 삭제요청시_해당카테고리가_없다면_예외_발생() {
        // given
        카테고리_조회에_실패한다();

        // when
        HoneyBreadException ex = assertThrows(HoneyBreadException.class, this::카테고리_삭제);

        // then
        카테고리_조회가_수행되어야_한다();
        카테고리_찾을수_없음_에러코드_확인(ex);
    }

    /**
     * Given
     */
    private void 중복되는_카테고리가_있다() {
        given(repository.existsByName(anyString())).willReturn(true);
    }

    private void 중복되는_카테고리가_없다() {
        given(repository.existsByName(anyString())).willReturn(false);
    }

    private void 카테고리_조회에_성공한다() {
        given(repository.findById(anyLong())).willReturn(Optional.of(category));
    }

    private void 카테고리_조회에_실패한다() {
        given(repository.findById(anyLong())).willReturn(Optional.empty());
    }

    /**
     * When
     */
    private void 카테고리_등록() {
        service.create(카테고리_요청("한식"));
    }

    private void 카테고리_수정() {
        service.update(anyLong(), 카테고리_요청("한식을 중식으로 수정"));
    }

    private void 카테고리_삭제() {
        service.delete(anyLong());
    }

    /**
     * Then
     */
    private void 중복_카테고리_확인이_수행되어야_한다() {
        then(repository).should().existsByName(anyString());
    }

    private void 카테고리_조회가_수행되어야_한다() {
        then(repository).should().findById(anyLong());
    }

    private void 중복_카테고리_에러코드_확인(HoneyBreadException ex) {
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CATEGORY);
    }

    private void 카테고리_찾을수_없음_에러코드_확인(HoneyBreadException ex) {
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    /**
     * Helper
     */
    private CategoryRequest 카테고리_요청(final String name) {
        return new CategoryRequest(name);
    }
}