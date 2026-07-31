package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CardPrintingRegistryTest {

    @BeforeEach
    @AfterEach
    void clearTokenImages() {
        CardPrintingRegistry.clearTokenImages();
    }

    @Test
    void prefersTokenFromSourceSet() {
        CardPrintingRegistry.registerTokenImages("AAA", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("taaa", "1")));
        CardPrintingRegistry.registerTokenImages("BBB", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("tbbb", "9")));

        CardPrintingRegistry.TokenImageData image =
                CardPrintingRegistry.getTokenImage("BBB", "Wolf", 2, 2, CardColor.GREEN);

        assertThat(image).isNotNull();
        assertThat(image.setCode()).isEqualTo("tbbb");
        assertThat(image.collectorNumber()).isEqualTo("9");
    }

    @Test
    void fallsBackToAnotherSetWhenSourceSetLacksToken() {
        CardPrintingRegistry.registerTokenImages("AAA", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("taaa", "1")));
        CardPrintingRegistry.registerTokenImages("BBB", Map.of());

        CardPrintingRegistry.TokenImageData image =
                CardPrintingRegistry.getTokenImage("BBB", "Wolf", 2, 2, CardColor.GREEN);

        assertThat(image).isNotNull();
        assertThat(image.setCode()).isEqualTo("taaa");
        assertThat(image.collectorNumber()).isEqualTo("1");
    }

    @Test
    void fallsBackForNonCreatureTokens() {
        CardPrintingRegistry.registerTokenImages("AAA", Map.of(
                CardPrintingRegistry.buildTokenKey("Treasure", null, null, null),
                new CardPrintingRegistry.TokenImageData("taaa", "3")));

        CardPrintingRegistry.TokenImageData image =
                CardPrintingRegistry.getTokenImage("ZZZ", "Treasure", null);

        assertThat(image).isNotNull();
        assertThat(image.setCode()).isEqualTo("taaa");
        assertThat(image.collectorNumber()).isEqualTo("3");
    }

    @Test
    void returnsNullWhenNoSetHasToken() {
        CardPrintingRegistry.registerTokenImages("AAA", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("taaa", "1")));

        assertThat(CardPrintingRegistry.getTokenImage("AAA", "Saproling", 1, 1, CardColor.GREEN))
                .isNull();
    }

    @Test
    void nullSourceSetStaysArtlessEvenWhenOtherSetsHaveToken() {
        CardPrintingRegistry.registerTokenImages("AAA", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("taaa", "1")));

        assertThat(CardPrintingRegistry.getTokenImage(null, "Wolf", 2, 2, CardColor.GREEN)).isNull();
    }

    @Test
    void preferredSetPhyrexianPrefixMatch() {
        CardPrintingRegistry.registerTokenImages("NPH", Map.of(
                CardPrintingRegistry.buildTokenKey("Golem", 3, 3, null),
                new CardPrintingRegistry.TokenImageData("tnph", "4")));

        CardPrintingRegistry.TokenImageData image =
                CardPrintingRegistry.getTokenImage("NPH", "Phyrexian Golem", 3, 3, null);

        assertThat(image).isNotNull();
        assertThat(image.setCode()).isEqualTo("tnph");
        assertThat(image.collectorNumber()).isEqualTo("4");
    }

    @Test
    void phyrexianPrefixFallsBackAcrossSets() {
        CardPrintingRegistry.registerTokenImages("NPH", Map.of(
                CardPrintingRegistry.buildTokenKey("Golem", 3, 3, null),
                new CardPrintingRegistry.TokenImageData("tnph", "4")));

        CardPrintingRegistry.TokenImageData image =
                CardPrintingRegistry.getTokenImage("MBS", "Phyrexian Golem", 3, 3, null);

        assertThat(image).isNotNull();
        assertThat(image.setCode()).isEqualTo("tnph");
    }

    @Test
    void fallbackPicksLowestSetCodeWhenMultipleSetsHaveToken() {
        CardPrintingRegistry.registerTokenImages("CCC", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("tccc", "3")));
        CardPrintingRegistry.registerTokenImages("AAA", Map.of(
                CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("taaa", "1")));
        CardPrintingRegistry.registerTokenImages("BBB", Map.of());

        CardPrintingRegistry.TokenImageData image =
                CardPrintingRegistry.getTokenImage("BBB", "Wolf", 2, 2, CardColor.GREEN);

        assertThat(image.setCode()).isEqualTo("taaa");
    }
}
