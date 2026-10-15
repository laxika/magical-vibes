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

@CardUsed({InfiniteHourglass.class, GrizzlyBears.class, AnimateArtifact.class})
class InfiniteHourglassTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a time counter on Infinite Hourglass")
    void upkeepTriggerAddsTimeCounter() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger does not trigger during an opponent's upkeep")
    void upkeepTriggerDoesNotAffectOpponentsUpkeep() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());

        advanceToUpkeep(player2);
        resolveAllTriggers();

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

    @Test
    @DisplayName("Controller can remove a counter during an opponent's upkeep and immediately reduce the boost")
    void controllerRemovesCounterDuringOpponentsUpkeep() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        hourglass.setCounterCount(CounterType.TIME, 2);
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        resolveAllTriggers();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent can activate during their own upkeep even when the artifact is tapped")
    void opponentRemovesCounterDuringOwnUpkeepFromTappedHourglass() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        hourglass.setCounterCount(CounterType.TIME, 1);
        hourglass.tap();
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isZero();
        assertThat(hourglass.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Two activations may be stacked with only one time counter remaining")
    void stackedActivationsRemoveAtMostAvailableCounters() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        hourglass.setCounterCount(CounterType.TIME, 1);
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Removing a counter in response to the upkeep trigger does not prevent its later addition")
    void activationRespondsToUpkeepTrigger() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple hourglasses add their own boosts and an activation removes only its source's counter")
    void multipleHourglassesUseIndependentCounterCounts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new InfiniteHourglass());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.TIME, 2);
        second.setCounterCount(CounterType.TIME, 3);
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
