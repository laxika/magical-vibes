package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AnimateArtifact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiniteHourglass.class, GrizzlyBears.class})
class InfiniteHourglassTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a time counter on Infinite Hourglass")
    void upkeepTriggerAddsTimeCounter() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // upkeep trigger goes on stack
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger does not trigger during an opponent's upkeep")
    void upkeepTriggerDoesNotAffectOpponentsUpkeep() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    @DisplayName("No boost while Infinite Hourglass has no time counters")
    void noBoostWithoutCounters() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());

        var bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Every creature gets +1/+0 for each time counter")
    void boostScalesWithTimeCounters() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        hourglass.setCounterCount(CounterType.TIME, 3);

        var bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.power()).isEqualTo(3);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent creatures are also boosted")
    void boostsOpponentCreatures() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        hourglass.setCounterCount(CounterType.TIME, 2);

        var bonus = gqs.computeStaticBonus(gd, opponentBears);
        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Controller may remove a time counter during their own upkeep")
    void controllerRemovesCounterDuringOwnUpkeep() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
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
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
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
    @DisplayName("Activation resolves harmlessly when Infinite Hourglass has no time counters")
    void activationWithNoCounterIsHarmless() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate outside an upkeep step")
    void cannotActivateOutsideUpkeep() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        hourglass.setCounterCount(CounterType.TIME, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @CardUsed(AnimateArtifact.class)
    @DisplayName("All creatures includes Infinite Hourglass when it becomes a creature")
    void animatedHourglassBoostsItself() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        hourglass.setCounterCount(CounterType.TIME, 2);

        Permanent animateArtifact = harness.addToBattlefieldAndReturn(player1, new AnimateArtifact());
        animateArtifact.setAttachedTo(hourglass.getId());

        assertThat(gqs.isCreature(gd, hourglass)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hourglass)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hourglass)).isEqualTo(4);
    }
}
