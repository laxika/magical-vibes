package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContaminatedDrink.class})
class ContaminatedDrinkTest extends BaseCardTest {

    @Test
    void drawsXCardsAndGivesHalfXRoundedUpRadCounters() {
        harness.setHand(player1, List.of(new ContaminatedDrink()));
        harness.setLibrary(player1, List.of(
                new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink(),
                new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 1", "2, 1", "4, 2", "5, 3"})
    void drawsChosenXAndRoundsRadCountersUp(int x, int expectedRadCounters) {
        harness.setHand(player1, List.of(new ContaminatedDrink()));
        harness.setLibrary(player1, List.of(
                new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink(),
                new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, x, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(x);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6 - x);
        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isEqualTo(expectedRadCounters);
        harness.assertInGraveyard(player1, "Contaminated Drink");
    }

    @Test
    void addsToControllersExistingRadCountersDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ContaminatedDrink()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(
                new ContaminatedDrink(), new ContaminatedDrink(), new ContaminatedDrink()));
        gd.playerRadCounters.put(player1.getId(), 4);
        gd.playerRadCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }
}
