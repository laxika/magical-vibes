package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreakwoodSafewrightTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters")
    void entersWithThreeMinusOneMinusOneCounters() {
        harness.setHand(player1, List.of(new CreakwoodSafewright()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent safewright = findPermanent(player1, "Creakwood Safewright");
        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a -1/-1 counter at your end step when an Elf is in your graveyard")
    void removesCounterWithElfInGraveyard() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not remove a counter when no Elf is in your graveyard")
    void doesNotRemoveCounterWithoutElfInGraveyard() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.<Card>of(new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when it has no -1/-1 counters")
    void doesNotTriggerWithoutMinusOneMinusOneCounter() {
        Permanent safewright = addSafewrightWithCounters(0);
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        advanceToEndStep(player1);

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private Permanent addSafewrightWithCounters(int count) {
        Permanent safewright = harness.addToBattlefieldAndReturn(player1, new CreakwoodSafewright());
        safewright.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, count);
        return safewright;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
