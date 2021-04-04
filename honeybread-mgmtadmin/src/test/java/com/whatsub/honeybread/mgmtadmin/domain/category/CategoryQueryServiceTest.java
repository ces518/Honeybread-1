package com.whatsub.honeybread.mgmtadmin.domain.category;

import com.whatsub.honeybread.core.domain.category.Category;
import com.whatsub.honeybread.core.domain.category.CategoryRepository;
import com.whatsub.honeybread.core.domain.category.dto.CategorySearch;
import com.whatsub.honeybread.core.infra.errors.ErrorCode;
import com.whatsub.honeybread.core.infra.exception.HoneyBreadException;
import com.whatsub.honeybread.mgmtadmin.domain.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(classes = CategoryQueryService.class)
@RequiredArgsConstructor
class CategoryQueryServiceTest {

    final CategoryQueryService queryService;

    @MockBean
    CategoryRepository repository;

    @Mock
    Category category;

    @Test
    void 검색_요청시_검색조건이_없다면_SIZE_만큼_조회_성공() {
        // given
        final int size = 10;
        final List<Category> mockCategories = 카테고리_목록_생성(size);

        PageRequest pageRequest = PageRequest.of(0, size);
        CategorySearch search = new CategorySearch();

        given(repository.getCategories(any(Pageable.class), any(CategorySearch.class)))
            .willReturn(new PageImpl<>(mockCategories, pageRequest, mockCategories.size()));

        // when
        Page<CategoryResponse> response = queryService.getCategories(pageRequest, search);

        // then
        카테고리_검색이_수행되어야_한다();

        assertThat(response.getTotalElements()).isEqualTo(size);
        assertThat(response.getContent().size()).isEqualTo(size);
    }

    @Test
    void 검색요청시_검색조건이_있다면_해당하는_목록_조회_성공() {
        // given
        final int size = 10;
        final List<Category> mockCategories = 카테고리_목록_생성(size);
        final List<Category> willSearchCategories = mockCategories.subList(1, 2);

        PageRequest pageRequest = PageRequest.of(0, size);
        CategorySearch search = new CategorySearch();
        search.setName("카테고리1");

        given(repository.getCategories(any(Pageable.class), any(CategorySearch.class)))
            .willReturn(new PageImpl<>(willSearchCategories, pageRequest, willSearchCategories.size()));

        // when
        Page<CategoryResponse> response = queryService.getCategories(pageRequest, search);

        // then
        카테고리_검색이_수행되어야_한다();

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().size()).isEqualTo(1);
    }

    @Test
    void 상세조회_요청시_해당카테고리가_있다면_조회_성공() {
        // given
        카테고리_조회시_성공한다();

        // when
        CategoryResponse response = 카테고리_조회();

        // then
        카테고리_조회가_수행되어야_한다();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getName()).isEqualTo(category.getName());
    }

    @Test
    void 상세조회_요청시_해당카테고리가_없다면_예외_발생() {
        // given
        카테고리_조회시_실패한다();

        // when
        HoneyBreadException ex = assertThrows(HoneyBreadException.class, this::카테고리_조회);

        // then
        카테고리_조회가_수행되어야_한다();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    /**
     * Given
     */
    private void 카테고리_조회시_성공한다() {
        given(category.getId()).willReturn(1L);
        given(category.getName()).willReturn("한식");
        given(repository.findById(anyLong())).willReturn(Optional.of(category));
    }

    private void 카테고리_조회시_실패한다() {
        given(repository.findById(anyLong())).willReturn(Optional.empty());
    }

    /**
     * When
     */
    private CategoryResponse 카테고리_조회() {
        return queryService.getCategory(anyLong());
    }

    /**
     * Then
     */
    private void 카테고리_검색이_수행되어야_한다() {
        then(repository).should().getCategories(any(Pageable.class), any(CategorySearch.class));
    }

    private void 카테고리_조회가_수행되어야_한다() {
        then(repository).should().findById(anyLong());
    }

    /**
     * Helper
     */
    private List<Category> 카테고리_목록_생성(final int size) {
        return LongStream.range(0, size)
            .mapToObj(id -> {
                Category mock = mock(Category.class);
                given(mock.getId()).willReturn(id);
                given(mock.getName()).willReturn("카테고리" + id);
                return mock;
            })
            .collect(Collectors.toList());
    }
}