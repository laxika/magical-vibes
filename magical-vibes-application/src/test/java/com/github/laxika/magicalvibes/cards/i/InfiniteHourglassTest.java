package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfiniteHourglassTest extends BaseCardTest {

    // ===== Upkeep trigger: time counters =====

    @Test
    @DisplayName("Upkeep trigger puts a time counter on Infinite Hourglass")
    void upkeepTriggerAddsTimeCounter() {
        Permanent hourglass = addHourglass(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // upkeep trigger goes on stack
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    // ===== Static boost scales with time counters =====

    @Test
    @DisplayName("No boost while Infinite Hourglass has no time counters")
    void noBoostWithoutCounters() {
        Permanent bears = addBears(player1);
        addHourglass(player1);

        var bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Every creature gets +1/+0 for each time counter")
    void boostScalesWithTimeCounters() {
        Permanent bears = addBears(player1);
        Permanent hourglass = addHourglass(player1);
        hourglass.setCounterCount(CounterType.TIME, 3);

        var bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.power()).isEqualTo(3);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent creatures are also boosted")
    void boostsOpponentCreatures() {
        Permanent opponentBears = addBears(player2);
        Permanent hourglass = addHourglass(player1);
        hourglass.setCounterCount(CounterType.TIME, 2);

        var bonus = gqs.computeStaticBonus(gd, opponentBears);
        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    // ===== Activated ability: remove a time counter =====

    @Test
    @DisplayName("Controller may remove a time counter during their own upkeep")
    void controllerRemovesCounterDuringOwnUpkeep() {
        Permanent hourglass = addHourglass(player1);
        hourglass.setCounterCount(CounterType.TIME, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may remove a time counter during another player's upkeep")
    void opponentRemovesCounterDuringControllerUpkeep() {
        Permanent hourglass = addHourglass(player1);
        hourglass.setCounterCount(CounterType.TIME, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate outside an upkeep step")
    void cannotActivateOutsideUpkeep() {
        Permanent hourglass = addHourglass(player1);
        hourglass.setCounterCount(CounterType.TIME, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    // ===== Helpers =====

    private Permanent addHourglass(Player owner) {
        Permanent perm = new Permanent(new InfiniteHourglass());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addBears(Player owner) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
