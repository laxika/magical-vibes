package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistortedCuriosity.class})
class DistortedCuriosityTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {U} when an opponent has three poison counters")
    void costsOneBlueWithCorrupted() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without an opponent having three poison counters")
    void doesNotGetReductionWithoutCorrupted() {
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller's poison counters do not enable Corrupted")
    void controllersPoisonCountersDoNotEnableCorrupted() {
        gd.playerPoisonCounters.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Distorted Curiosity draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Two opposing poison counters do not reduce the cost")
    void twoPoisonCountersDoNotEnableCorrupted() {
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Corrupted does not remove the blue mana requirement")
    void corruptedStillRequiresBlueMana() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("More than three poison counters enable the discount and drawing two cards")
    void drawsTwoCardsWithMoreThanThreePoisonCounters() {
        gd.playerPoisonCounters.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new DistortedCuriosity()));
        harness.setLibrary(player1, List.of(new DistortedCuriosity(), new DistortedCuriosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
