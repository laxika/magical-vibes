package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Darkpact.class, GrizzlyBears.class, HillGiant.class})
class DarkpactTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges an owned ante card with the top card of the library")
    void exchangesAnteCardWithLibraryTop() {
        Card antedCard = new GrizzlyBears();
        Card libraryTop = new HillGiant();
        gd.addToAnte(player1.getId(), antedCard);
        harness.setLibrary(player1, List.of(libraryTop));
        Card spell = new Darkpact();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.exiledCards.stream().filter(entry -> player1.getId().equals(entry.ownerId())
                && gd.antedCardIds.contains(entry.card().getId())).map(entry -> entry.card()).toList()).extracting(Card::getId)
                .containsExactly(libraryTop.getId());
        assertThat(gd.antedCardIds).containsExactly(libraryTop.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Leaves the ante card in the ante when there is no library card to exchange")
    void cannotExchangeAnteCardWithEmptyLibrary() {
        Card antedCard = new GrizzlyBears();
        gd.addToAnte(player1.getId(), antedCard);
        harness.setLibrary(player1, List.of());
        Card spell = new Darkpact();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().filter(entry -> player1.getId().equals(entry.ownerId())
                && gd.antedCardIds.contains(entry.card().getId())).map(entry -> entry.card()).toList()).extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.antedCardIds).containsExactly(antedCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Cannot target a card that is not in the ante")
    void cannotTargetRegularExiledCard() {
        Card exiledCard = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledCard));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new Darkpact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ante");
    }

    @Test
    @DisplayName("Takes ownership of an opponent's ante card and exchanges it")
    void exchangesOpponentsAnteCard() {
        Card antedCard = new GrizzlyBears();
        Card libraryTop = new HillGiant();
        gd.addToAnte(player2.getId(), antedCard);
        harness.setLibrary(player1, List.of(libraryTop));
        harness.setHand(player1, List.of(new Darkpact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().filter(entry -> player1.getId().equals(entry.ownerId())
                && gd.antedCardIds.contains(entry.card().getId())).map(entry -> entry.card()).toList()).extracting(Card::getId)
                .containsExactly(libraryTop.getId());
        assertThat(gd.antedCardIds).containsExactly(libraryTop.getId());
    }

    @Test
    @DisplayName("Takes ownership even when an empty library prevents the exchange")
    void takesOwnershipWithoutExchangeWhenLibraryIsEmpty() {
        Card antedCard = new GrizzlyBears();
        gd.addToAnte(player2.getId(), antedCard);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Darkpact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().filter(entry -> player1.getId().equals(entry.ownerId())
                && gd.antedCardIds.contains(entry.card().getId())).map(entry -> entry.card()).toList()).extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.findExiledCard(antedCard.getId()).ownerId()).isEqualTo(player1.getId());
        assertThat(gd.antedCardIds).containsExactly(antedCard.getId());
    }

    @Test
    @DisplayName("Exchanges only the chosen ante card and preserves the rest of the library")
    void preservesOtherAnteCardsAndLibraryOrder() {
        Card antedCard = new GrizzlyBears();
        Card otherAnteCard = new HillGiant();
        Card libraryTop = new HillGiant();
        Card libraryBottom = new GrizzlyBears();
        gd.addToAnte(player1.getId(), antedCard);
        gd.addToAnte(player1.getId(), otherAnteCard);
        harness.setLibrary(player1, List.of(libraryTop, libraryBottom));
        harness.setHand(player1, List.of(new Darkpact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(antedCard.getId(), libraryBottom.getId());
        assertThat(gd.exiledCards.stream().filter(entry -> player1.getId().equals(entry.ownerId())
                && gd.antedCardIds.contains(entry.card().getId())).map(entry -> entry.card()).toList()).extracting(Card::getId)
                .containsExactlyInAnyOrder(otherAnteCard.getId(), libraryTop.getId());
        assertThat(gd.antedCardIds).containsExactlyInAnyOrder(otherAnteCard.getId(), libraryTop.getId());
    }

    @Test
    @DisplayName("Does not exchange when the target leaves the ante before resolution")
    void targetLeavesAnteBeforeResolution() {
        Card antedCard = new GrizzlyBears();
        Card libraryTop = new HillGiant();
        gd.addToAnte(player1.getId(), antedCard);
        harness.setLibrary(player1, List.of(libraryTop));
        Card spell = new Darkpact();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, antedCard.getId());

        gd.removeFromExile(antedCard.getId());
        harness.setGraveyard(player1, List.of(antedCard));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryTop.getId());
        assertThat(gd.exiledCards.stream().filter(entry -> player1.getId().equals(entry.ownerId())
                && gd.antedCardIds.contains(entry.card().getId())).map(entry -> entry.card()).toList()).isEmpty();
        assertThat(gd.antedCardIds).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(antedCard, spell);
    }
}
