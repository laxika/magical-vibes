package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpaceTimeAnomaly.class})
class SpaceTimeAnomalyTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills cards equal to the caster's life total")
    void millsCardsEqualToCasterLifeTotal() {
        harness.setLife(player1, 6);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpaceTimeAnomaly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setLibrary(player2, library(10));

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The mill amount uses the controller's life total at resolution")
    void usesLifeTotalAtResolution() {
        harness.setLife(player1, 6);
        harness.setLibrary(player2, library(10));
        prepareSpell();

        harness.castSorcery(player1, 0, player2.getId());
        harness.setLife(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The controller can target themselves and mills the top cards")
    void canTargetSelf() {
        harness.setLife(player1, 2);
        List<Card> cards = library(4);
        harness.setLibrary(player1, cards);
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(cards.subList(2, 4));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(cards.subList(0, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player with fewer cards than the mill amount mills their entire library")
    void millsEntireShortLibrary() {
        harness.setLife(player1, 6);
        List<Card> cards = library(2);
        harness.setLibrary(player2, cards);
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Targeting a player with an empty library does not make them lose")
    void emptyLibraryDoesNotCauseLoss() {
        harness.setLibrary(player2, List.of());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SpaceTimeAnomaly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private List<Card> library(int size) {
        return IntStream.range(0, size).mapToObj(i -> (Card) new SpaceTimeAnomaly()).toList();
    }
}
