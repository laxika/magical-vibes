package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmageddonClockTest extends BaseCardTest {

    // ===== Upkeep trigger: doom counters =====

    @Test
    @DisplayName("Upkeep trigger puts a doom counter on Armageddon Clock")
    void upkeepTriggerAddsDoomCounter() {
        Permanent clock = addClock(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires upkeep trigger
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    // ===== Draw step: damage to each player equal to doom counters =====

    @Test
    @DisplayName("Draw step deals damage equal to doom counters to each player")
    void drawStepDealsDamageToEachPlayer() {
        Permanent clock = addClock(player1);
        clock.setCounterCount(CounterType.DOOM, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve draw-step trigger

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("With no doom counters the draw step deals no damage")
    void drawStepWithoutCountersDealsNoDamage() {
        addClock(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToDraw(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    // ===== Activated ability: {4} remove a doom counter =====

    @Test
    @DisplayName("Controller may remove a doom counter during their own upkeep")
    void controllerRemovesCounterDuringOwnUpkeep() {
        Permanent clock = addClock(player1);
        clock.setCounterCount(CounterType.DOOM, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may remove a doom counter during another player's upkeep")
    void opponentRemovesCounterDuringControllerUpkeep() {
        Permanent clock = addClock(player1);
        clock.setCounterCount(CounterType.DOOM, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the remove-counter ability outside an upkeep step")
    void cannotActivateOutsideUpkeep() {
        Permanent clock = addClock(player1);
        clock.setCounterCount(CounterType.DOOM, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    // ===== Helpers =====

    private Permanent addClock(Player owner) {
        Permanent perm = new Permanent(new ArmageddonClock());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip / empty-library loss
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UPKEEP -> DRAW fires draw-step trigger
    }
}
