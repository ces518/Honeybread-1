package com.whatsub.honeybread.mgmtadmin.domain.advertisement;

import com.whatsub.honeybread.core.domain.advertisement.AdvertisementBidNotice;
import com.whatsub.honeybread.core.domain.advertisement.AdvertisementType;
import com.whatsub.honeybread.core.domain.advertisement.dto.AdvertisementBidNoticeSearch;
import com.whatsub.honeybread.core.domain.advertisement.repository.AdvertisementBidNoticeRepository;
import com.whatsub.honeybread.core.domain.model.Money;
import com.whatsub.honeybread.core.domain.model.TimePeriod;
import com.whatsub.honeybread.mgmtadmin.domain.advertisement.dto.AdvertisementBidNoticeResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(classes = AdvertisementBidNoticeQueryService.class)
@RequiredArgsConstructor
class AdvertisementBidNoticeQueryServiceTest {

    final AdvertisementBidNoticeQueryService queryService;

    final int size = 10;

    @MockBean
    AdvertisementBidNoticeRepository repository;

    @Test
    void 검색_요청시_검색조건이_없다면_SIZE_만큼_조회_성공() {
        // given
        입찰공고_검색시_성공한다();

        // when
        Page<AdvertisementBidNoticeResponse> advertiseBidNoticePage = 입찰공고_검색();

        // then
        입찰공고_검색이_수행되어야_한다();
        assertThat(advertiseBidNoticePage.getNumberOfElements()).isEqualTo(size);
    }

    /**
     * Given
     */
    private void 입찰공고_검색시_성공한다() {
        List<AdvertisementBidNotice> advertisementBidNotices = 입찰_공고_생성();
        given(repository.findAll(any(Pageable.class), any(AdvertisementBidNoticeSearch.class)))
            .willReturn(new PageImpl<>(advertisementBidNotices, PageRequest.of(0, size), size));
    }

    /**
     * When
     */
    private Page<AdvertisementBidNoticeResponse> 입찰공고_검색() {
        PageRequest pageRequest = PageRequest.of(0, size);
        AdvertisementBidNoticeSearch search = new AdvertisementBidNoticeSearch();
        return queryService.findAll(pageRequest, search);
    }

    /**
     * Then
     */
    private void 입찰공고_검색이_수행되어야_한다() {
        then(repository).should().findAll(any(Pageable.class), any(AdvertisementBidNoticeSearch.class));
    }

    /**
     * Helper
     */
    private List<AdvertisementBidNotice> 입찰_공고_생성() {
        return LongStream.range(0, size)
            .mapToObj(id -> {
                AdvertisementBidNotice mock = mock(AdvertisementBidNotice.class);
                given(mock.getId()).willReturn(id);
                given(mock.getStatus()).willReturn(AdvertisementBidNotice.Status.OPEN);
                given(mock.getMinimumBidPrice()).willReturn(Money.wons(10000));
                given(mock.getBidPriceUnit()).willReturn(Money.wons(10000));
                given(mock.getMaximumStoreCounts()).willReturn(50);
                given(mock.getType()).willReturn(AdvertisementType.OPEN_LIST);
                given(mock.getPeriod()).willReturn(TimePeriod.of(
                    LocalDateTime.of(2021, 4, 12, 12, 0, 0),
                    LocalDateTime.of(2021, 6, 12, 12, 0, 0)
                ));
                given(mock.getCreatedAt()).willReturn(LocalDateTime.now());
                given(mock.getLastModifiedAt()).willReturn(LocalDateTime.now());
                return mock;
            }).collect(Collectors.toList());
    }
}