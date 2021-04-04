package com.whatsub.honeybread.mgmtadmin.domain.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsub.honeybread.core.domain.category.dto.CategorySearch;
import com.whatsub.honeybread.core.infra.errors.ErrorCode;
import com.whatsub.honeybread.core.infra.exception.HoneyBreadException;
import com.whatsub.honeybread.mgmtadmin.domain.category.dto.CategoryRequest;
import com.whatsub.honeybread.mgmtadmin.domain.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(CategoryController.class)
@RequiredArgsConstructor
class CategoryControllerTest {

    static final String BASE_URL = "/categories";

    final MockMvc mockMvc;

    final ObjectMapper mapper;

    @MockBean
    CategoryService service;

    @MockBean
    CategoryQueryService queryService;

    @Mock
    CategoryResponse response;

    @Test
    void 검색_요청시_검색조건이_없다면_SIZE_만큼_조회에_성공한다() throws Exception {
        // given
        final int size = 10;
        final List<CategoryResponse> mockCategories = generateMockCategories(size);

        PageRequest pageRequest = PageRequest.of(0, size);

        given(queryService.getCategories(any(Pageable.class), any(CategorySearch.class)))
            .willReturn(new PageImpl<>(mockCategories, pageRequest, mockCategories.size()));

        // when
        ResultActions result = 카테고리_검색();

        // then
        카테고리_검색이_수행되어야_한다();

        Ok응답_확인(result);
        검색_응답_필드_확인(size, result);
    }

    @Test
    void 검색_요청시_검색조건이_있다면_해당하는_목록_조회에_성공한다() throws Exception {
        // given
        final int size = 10;
        final List<CategoryResponse> mockCategories = generateMockCategories(size);
        final List<CategoryResponse> willSearchCategories = mockCategories.subList(1, 2);

        PageRequest pageRequest = PageRequest.of(0, size);
        CategorySearch search = new CategorySearch();
        search.setName("카테고리1");

        given(queryService.getCategories(any(Pageable.class), any(CategorySearch.class)))
            .willReturn(new PageImpl<>(willSearchCategories, pageRequest, willSearchCategories.size()));

        // when
        ResultActions result = 카테고리_검색();

        // then
        카테고리_검색이_수행되어야_한다();

        Ok응답_확인(result);
        검색_응답_필드_확인(willSearchCategories.size(), result);
    }

    @Test
    void 카테고리가_존재한다면_조회에_성공한다() throws Exception {
        // given
        카테고리_조회시_성공한다();

        // when
        ResultActions result = 카테고리_조회();

        // then
        카테고리_조회가_수행되어야_한다();

        Ok응답_확인(result);
        result.andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.name").value(response.getName()));
    }

    @Test
    void 카테고리가_존재하지_않는다면_조회에_실패한다() throws Exception {
        // given
        카테고리_조회시_실패한다();

        // when
        ResultActions result = 카테고리_조회();

        // then
        카테고리_조회가_수행되어야_한다();
        NotFound_응답_확인(result);
    }

    @Test
    void 중복되는_카테고리가_없다면_등록에_성공한다() throws Exception {
        // given
        카테고리_등록시_성공한다();

        // when
        ResultActions result = 카테고리_등록();

        // then
        카테고리_등록이_수행되어야_한다();
        Created_응답_확인(result);
    }

    @Test
    void 중복되는_카테고리가_있다면_등록에_실패한다() throws Exception {
        // given
        카테고리_등록시_중복_카테고리가_존재한다();

        // when
        ResultActions result = 카테고리_등록();

        // then
        카테고리_등록이_수행되어야_한다();
        BadRequest_응답_확인(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "한",
        "한식중식양식일식abcdefghijklmnopqrstuvwxyz123456789010가나다라마바사아자차카"
    })
    void 카테고리명이_2글자_미만_50자초과라면_등록에_실패한다(String name) throws Exception {
        // when
        ResultActions result = mockMvc.perform(
            post(BASE_URL)
                .content(mapper.writeValueAsString(카테고리_요청(name)))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        BadRequest_응답_확인(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "한식",
        "한식123",
        "한식12345678910",
        "한식중식양식일식abcdefghijklmnopqrstuvwxyz123456789010가나다"
    })
    void 카테고리명이_2글자_이상_50자_이하라면_등록에_성공한다(String name) throws Exception {
        // given
        카테고리_등록시_성공한다();

        // when
        ResultActions result = mockMvc.perform(
            post(BASE_URL)
                .content(mapper.writeValueAsString(카테고리_요청(name)))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        카테고리_등록이_수행되어야_한다();
        Created_응답_확인(result);
    }

    @Test
    void 중복되는_카테고리가_없다면_수정에_성공한다() throws Exception {
        // given
        카테고리_수정시_성공한다();

        // when
        ResultActions result = 카테고리_수정();

        // then
        카테고리_수정이_수행되어야_한다();
        Ok응답_확인(result);
    }

    @Test
    void 중복되는_카테고리가_있다면_수정에_실패한다() throws Exception {
        // given
        카테고리_수정시_중복_카테고리가_존재한다();

        // when
        ResultActions result = 카테고리_수정();

        // then
        카테고리_수정이_수행되어야_한다();
        Conflict_응답_확인(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "한",
        "한식중식양식일식abcdefghijklmnopqrstuvwxyz123456789010가나다라마바사아자차카"
    })
    void 카테고리명이_2글자_미만_50자_초과라면_수정에_실패한다(String name) throws Exception {
        // when
        ResultActions result = mockMvc.perform(
            put(BASE_URL + "/1")
                .content(mapper.writeValueAsString(카테고리_요청(name)))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        BadRequest_응답_확인(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "한식",
        "한식123",
        "한식12345678910",
        "한식중식양식일식abcdefghijklmnopqrstuvwxyz123456789010가나다"
    })
    void 카테고리명이_2글자_이상_50자_이하라면_수정에_성공한다(String name) throws Exception {
        // given
        카테고리_수정시_성공한다();

        // when
        ResultActions result = mockMvc.perform(
            put(BASE_URL + "/1")
                .content(mapper.writeValueAsString(카테고리_요청(name)))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        카테고리_수정이_수행되어야_한다();
        Ok응답_확인(result);
    }

    @Test
    void 카테고리가_존재하지_않는다면_수정에_실패한다() throws Exception {
        // given
        카테고리_수정시_카테고리가_존재하지_않는다();

        // when
        ResultActions result = 카테고리_수정();
        // then
        카테고리_수정이_수행되어야_한다();
        NotFound_응답_확인(result);
    }

    @Test
    void 카테고리가_존재할경우_삭제에_성공한다() throws Exception {
        // given
        willDoNothing().given(service).delete(anyLong());

        // when
        ResultActions result = 카테고리_삭제();

        // then
        카테고리_삭제가_수행되어야_한다();
        NoContent_응답_확인(result);
    }

    @Test
    void 카테고리가_존재하지_않을경우_삭제에_실패한다() throws Exception {
        // given
        willThrow(new HoneyBreadException(ErrorCode.CATEGORY_NOT_FOUND)).given(service).delete(anyLong());

        // when
        ResultActions result = 카테고리_삭제();

        // then
        카테고리_삭제가_수행되어야_한다();
        NotFound_응답_확인(result);
    }

    /**
     * Given
     */
    private void 카테고리_조회시_성공한다() {
        given(response.getId()).willReturn(1L);
        given(response.getName()).willReturn("한식");
        given(queryService.getCategory(anyLong())).willReturn(response);
    }

    private void 카테고리_조회시_실패한다() {
        given(queryService.getCategory(anyLong())).willThrow(new HoneyBreadException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void 카테고리_등록시_성공한다() {
        given(service.create(any(CategoryRequest.class))).willReturn(Long.MIN_VALUE);
    }

    private void 카테고리_등록시_중복_카테고리가_존재한다() {
        given(service.create(any(CategoryRequest.class)))
            .willThrow(new HoneyBreadException(ErrorCode.DUPLICATE_CATEGORY));
    }

    private void 카테고리_수정시_성공한다() {
        willDoNothing().given(service).update(anyLong(), any(CategoryRequest.class));
    }

    private void 카테고리_수정시_중복_카테고리가_존재한다() {
        willThrow(new HoneyBreadException(ErrorCode.DUPLICATE_CATEGORY))
            .given(service).update(anyLong(), any(CategoryRequest.class));
    }

    private void 카테고리_수정시_카테고리가_존재하지_않는다() {
        willThrow(new HoneyBreadException(ErrorCode.CATEGORY_NOT_FOUND))
            .given(service).update(anyLong(), any(CategoryRequest.class));
    }

    /**
     * When
     */
    private ResultActions 카테고리_검색() throws Exception {
        return mockMvc.perform(
            get(BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
    }

    private ResultActions 카테고리_조회() throws Exception {
        ResultActions result = mockMvc.perform(
            get(BASE_URL + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
        return result;
    }

    private ResultActions 카테고리_등록() throws Exception {
        return mockMvc.perform(
            post(BASE_URL)
                .content(mapper.writeValueAsString(카테고리_요청("한식")))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
    }

    private ResultActions 카테고리_수정() throws Exception {
        return mockMvc.perform(
            put(BASE_URL + "/1")
                .content(mapper.writeValueAsString(카테고리_요청("한식을 중식으로 수정")))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
    }

    private ResultActions 카테고리_삭제() throws Exception {
        return mockMvc.perform(
            delete(BASE_URL + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
    }


    /**
     * Then
     */
    private void 카테고리_검색이_수행되어야_한다() {
        then(queryService).should().getCategories(any(Pageable.class), any(CategorySearch.class));
    }

    private void 카테고리_조회가_수행되어야_한다() {
        then(queryService).should().getCategory(anyLong());
    }

    private void 카테고리_등록이_수행되어야_한다() {
        then(service).should().create(any(CategoryRequest.class));
    }

    private void 카테고리_수정이_수행되어야_한다() {
        then(service).should().update(anyLong(), any(CategoryRequest.class));
    }

    private void 카테고리_삭제가_수행되어야_한다() {
        then(service).should().delete(anyLong());
    }

    private void 검색_응답_필드_확인(int size, ResultActions result) throws Exception {
        result.andExpect(jsonPath("$.content").isNotEmpty())
            .andExpect(jsonPath("$.content.length()").value(size))
            .andExpect(jsonPath("$.content[0].id").exists())
            .andExpect(jsonPath("$.content[0].name").exists());
    }

    private void Ok응답_확인(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void Created_응답_확인(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    private void NoContent_응답_확인(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void BadRequest_응답_확인(ResultActions result) throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    private void NotFound_응답_확인(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound());
    }

    private void Conflict_응답_확인(ResultActions result) throws Exception {
        result.andExpect(status().isConflict());
    }


    /**
     * Helper
     */
    private CategoryRequest 카테고리_요청(String name) {
        return new CategoryRequest(name);
    }

    private List<CategoryResponse> generateMockCategories(final int size) {
        return LongStream.range(0, size)
            .mapToObj(id -> CategoryResponse.builder()
                .id(id)
                .name("카테고리" + id)
                .build())
            .collect(Collectors.toList());
    }
}