package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.s.SteadyProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ChimericMass.class, SteadyProgress.class})
class ChimericMassTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Chimeric Mass with X=3 enters with 3 charge counters")
    void entersWith3ChargeCounters() {
        harness.setHand(player1, List.of(new ChimericMass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent mass = findMass(player1);
        assertThat(mass).isNotNull();
        assertThat(mass.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting Chimeric Mass with X=0 enters with 0 charge counters")
    void entersWith0ChargeCounters() {
        harness.setHand(player1, List.of(new ChimericMass()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent mass = findMass(player1);
        assertThat(mass).isNotNull();
        assertThat(mass.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting Chimeric Mass with X=5 enters with 5 charge counters")
    void entersWith5ChargeCounters() {
        harness.setHand(player1, List.of(new ChimericMass()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCard(gd, player1, 0, 5, null, null);
        harness.passBothPriorities();

        Permanent mass = findMass(player1);
        assertThat(mass).isNotNull();
        assertThat(mass.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Chimeric Mass is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent mass = addMassReady(player1, 3);

        assertThat(gqs.isCreature(gd, mass)).isFalse();
    }

    @Test
    @DisplayName("Activating ability with 3 charge counters makes it a 3/3 creature")
    void animateWith3CountersMakesIt3x3() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, mass)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating ability with 5 charge counters makes it a 5/5 creature")
    void animateWith5CountersMakesIt5x5() {
        Permanent mass = addMassReady(player1, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animating with zero charge counters puts Chimeric Mass into the graveyard")
    void animateWith0CountersMakesIt0x0() {
        addMassReady(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chimeric Mass");
        harness.assertInGraveyard(player1, "Chimeric Mass");
    }

    @Test
    @DisplayName("Gains Construct creature subtype when animated")
    void gainsConstructSubtypeWhenAnimated() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(mass.getTransientSubtypes()).isEmpty();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mass.getTransientSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
    }

    @Test
    @DisplayName("Charge counters persist after animation ends at end of turn")
    void chargeCountersPersistAfterAnimationEnds() {
        Permanent mass = addMassReady(player1, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(mass.getCounterCount(CounterType.CHARGE)).isEqualTo(4);

        // Advance to cleanup step — animation ends
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, mass)).isFalse();
        // Charge counters should still be there
        assertThat(mass.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Re-activating in same turn uses same charge counters")
    void reactivatingUsesSameChargeCounters() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);

        // Activate again — P/T should still be 3 (same charge counters)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation resets at end of turn — reverts to non-creature artifact")
    void animationResetsAtEndOfTurn() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mass)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, mass)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mass.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Animated power and toughness follow charge counters added by proliferate")
    void proliferatingAnimatedMassIncreasesPowerAndToughness() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(mass.getId()));

        assertThat(mass.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(4);
    }

    @Test
    @DisplayName("Animation affects only its source and waits for resolution")
    void animationAffectsOnlySourceAfterResolution() {
        Permanent mass = addMassReady(player1, 3);
        Permanent other = addMassReady(player1, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.isCreature(gd, mass)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mass)).isTrue();
        assertThat(gqs.isCreature(gd, other)).isFalse();
        assertThat(other.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }

    private Permanent addMassReady(Player player, int chargeCounters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChimericMass());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.CHARGE, chargeCounters);
        return perm;
    }

    private Permanent findMass(Player player) {
        return findPermanent(player, "Chimeric Mass");
    }
}
