package net.javaguides.reactivebeerclient.client;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.reactivebeerclient.config.WebClientConfig;
import net.javaguides.reactivebeerclient.model.BeerDto;
import net.javaguides.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());
    }

    @Test
    void getBeerById() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null,
                null, false);
        final BeerPagedList beerPagedList = beerPagedListMono.block();
        final UUID beerId = beerPagedList.getContent().get(0).getId();

        final Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, false);
        final BeerDto beerDto = beerDtoMono.block();

        log.info("beerDto = " + beerDto);

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getId()).isEqualTo(beerId);
    }

    @Test
    void listBeers() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null,
                null, false);

        final BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isGreaterThan(0);

        log.info("beerPagedList = " + beerPagedList.toList());
    }

    @Test
    void listBeersSize10() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10, null,
                null, false);
        final BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isEqualTo(10);

        log.info("beerPagedList = " + beerPagedList.toList());
    }

    @Test
    void listBeersNoRecords() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20, null,
                null, false);
        final BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isEqualTo(0);

        log.info("beerPagedList = " + beerPagedList.toList());
    }

    @Test
    void createBeer() {
    }

    @Test
    void updateBeer() {
    }

    @Test
    void deleteBeerById() {
    }

    @Test
    void getBeerByUPC() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null,
                null, false);
        final BeerPagedList beerPagedList = beerPagedListMono.block();
        final String upc = beerPagedList.getContent().get(0).getUpc();

        final Mono<BeerDto> beerDtoMono = beerClient.getBeerByUPC(upc);
        final BeerDto beerDto = beerDtoMono.block();

        log.info("beerDto = " + beerDto);

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getUpc()).isEqualTo(upc);
    }
}