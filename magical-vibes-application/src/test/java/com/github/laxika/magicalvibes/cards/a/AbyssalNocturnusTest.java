package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cryptwailing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbyssalNocturnus.class, Cryptwailing.class})
class AbyssalNocturnusTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discarding a card gives it +2/+2 and fear")
    void opponentDiscardBoostsAndGrantsFear() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        harness.setHand(player2, new ArrayList<>(List.of(new AbyssalNocturnus())));
        readyCryptwailingMana(1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(nocturnus.getPowerModifier()).isEqualTo(2);
        assertThat(nocturnus.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Each opponent discard stacks another boost")
    void opponentDiscardsStack() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        harness.setHand(player2, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        readyCryptwailingMana(2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        readyCryptwailingMana(0);
        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(nocturnus.getPowerModifier()).isEqualTo(4);
        assertThat(nocturnus.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost and fear wear off at end of turn")
    void boostAndFearWearOffAtEndOfTurn() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        harness.setHand(player2, new ArrayList<>(List.of(new AbyssalNocturnus())));
        readyCryptwailingMana(1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(nocturnus.getPowerModifier()).isZero();
        assertThat(nocturnus.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("A controller discard does not trigger it")
    void controllerDiscardDoesNotTrigger() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        harness.setHand(player1, new ArrayList<>(List.of(new AbyssalNocturnus())));
        readyCryptwailingMana(1);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(nocturnus.getPowerModifier()).isZero();
        assertThat(nocturnus.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Each copy triggers independently for the same opponent discard")
    void eachCopyGetsItsOwnBoostAndFear() {
        Permanent first = addCreatureReady(player1, new AbyssalNocturnus());
        Permanent second = addCreatureReady(player1, new AbyssalNocturnus());
        Permanent opponentsCopy = addCreatureReady(player2, new AbyssalNocturnus());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        harness.setHand(player2, new ArrayList<>(List.of(new AbyssalNocturnus())));
        readyCryptwailingMana(1);

        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        for (Permanent nocturnus : List.of(first, second)) {
            assertThat(nocturnus.getPowerModifier()).isEqualTo(2);
            assertThat(nocturnus.getToughnessModifier()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isTrue();
        }
        assertThat(opponentsCopy.getPowerModifier()).isZero();
        assertThat(opponentsCopy.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opponentsCopy, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("An attempted discard from an empty hand does not trigger")
    void emptyHandDoesNotTrigger() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new AbyssalNocturnus(), new AbyssalNocturnus())));
        harness.setHand(player2, new ArrayList<>());
        readyCryptwailingMana(1);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(nocturnus.getPowerModifier()).isZero();
        assertThat(nocturnus.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isFalse();
    }

    private void readyCryptwailingMana(int amount) {
        harness.addMana(player1, ManaColor.COLORLESS, amount);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
