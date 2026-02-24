package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXChargeCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChimericMassTest extends BaseCardTest {


    // ===== Card properties =====


    @Test
    @DisplayName("Chimeric Mass has EnterWithXChargeCountersEffect and AnimateSelfByChargeCounters ability")
    void hasCorrectProperties() {
        ChimericMass card = new ChimericMass();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithXChargeCountersEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateSelfByChargeCountersEffect.class);
    }

    // ===== Casting with X charge counters =====

    @Test
    @DisplayName("Casting Chimeric Mass with X=3 enters with 3 charge counters")
    void entersWith3ChargeCounters() {
        harness.setHand(player1, List.of(new ChimericMass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent mass = findMass(player1);
        assertThat(mass).isNotNull();
        assertThat(mass.getChargeCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting Chimeric Mass with X=0 enters with 0 charge counters")
    void entersWith0ChargeCounters() {
        harness.setHand(player1, List.of(new ChimericMass()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent mass = findMass(player1);
        assertThat(mass).isNotNull();
        assertThat(mass.getChargeCounters()).isEqualTo(0);
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
        assertThat(mass.getChargeCounters()).isEqualTo(5);
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Chimeric Mass is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent mass = addMassReady(player1, 3);

        assertThat(gqs.isCreature(gd, mass)).isFalse();
        assertThat(mass.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    // ===== Activated ability: animate by charge counters =====

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
    @DisplayName("Activating ability with 0 charge counters makes it a 0/0 creature")
    void animateWith0CountersMakesIt0x0() {
        Permanent mass = addMassReady(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(0);
    }

    // ===== Gains Construct subtype =====

    @Test
    @DisplayName("Gains Construct creature subtype when animated")
    void gainsConstructSubtypeWhenAnimated() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(mass.getGrantedSubtypes()).isEmpty();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mass.getGrantedSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
    }

    // ===== Charge counters persist =====

    @Test
    @DisplayName("Charge counters persist after animation ends at end of turn")
    void chargeCountersPersistAfterAnimationEnds() {
        Permanent mass = addMassReady(player1, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(mass.getChargeCounters()).isEqualTo(4);

        // Advance to cleanup step — animation ends
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mass.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, mass)).isFalse();
        // Charge counters should still be there
        assertThat(mass.getChargeCounters()).isEqualTo(4);
    }

    // ===== Re-animation uses current charge counters =====

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

    // ===== End of turn resets animation =====

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

    // ===== Does not tap to activate =====

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent mass = addMassReady(player1, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mass.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addMassReady(Player player, int chargeCounters) {
        Permanent perm = new Permanent(new ChimericMass());
        perm.setSummoningSick(false);
        perm.setChargeCounters(chargeCounters);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findMass(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chimeric Mass"))
                .findFirst().orElse(null);
    }
}
