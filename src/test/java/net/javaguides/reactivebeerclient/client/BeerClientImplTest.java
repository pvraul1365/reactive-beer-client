package net.javaguides.reactivebeerclient.client;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.reactivebeerclient.config.WebClientConfig;
import net.javaguides.reactivebeerclient.model.BeerDto;
import net.javaguides.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        BeerDto beerDto = BeerDto.builder()
                .beerName("DogFish Head 90 Minute IPA")
                .beerStyle("IPA")
                .upc("123456789012")
                .price(new BigDecimal("10.99"))
                .build();

        final Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beerDto);
        final ResponseEntity responseEntity = responseEntityMono.block();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateBeer() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null,
                null, false);
        final BeerPagedList beerPagedList = beerPagedListMono.block();
        final BeerDto beerDto = beerPagedList.getContent().get(0);

        log.info("beerDto = " + beerDto);

        final BeerDto updatedBeerDto = BeerDto.builder()
                .beerName("Really Good Beer")
                .beerStyle(beerDto.getBeerStyle())
                .upc(beerDto.getUpc())
                .price(beerDto.getPrice())
                .build();

        final Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(beerDto.getId(), updatedBeerDto);
        final ResponseEntity<Void> responseEntity = responseEntityMono.block();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerById() {
        final Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null,
                null, false);
        final BeerPagedList beerPagedList = beerPagedListMono.block();
        final UUID beerId = beerPagedList.getContent().get(0).getId();

        final Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerId);
        final ResponseEntity<Void> responseEntity = responseEntityMono.block();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
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

    @Test
    void deleteBeerHandleException() {
        final UUID beerId = UUID.randomUUID();
        final Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerId);
        final ResponseEntity<Void> responseEntity = responseEntityMono
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error deleting beer: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteBeerByIdNotFound() {
        final UUID beerId = UUID.randomUUID();

        final Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerId);

        assertThrows(WebClientResponseException.class, () -> {
            final ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }
}