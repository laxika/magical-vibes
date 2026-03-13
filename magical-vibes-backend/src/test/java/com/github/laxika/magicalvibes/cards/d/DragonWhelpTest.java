package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ActivationCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DragonWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Dragon Whelp has pump ability and conditional end-step sacrifice")
    void hasCorrectProperties() {
        DragonWhelp card = new DragonWhelp();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{R}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);

        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(ActivationCountConditionalEffect.class);
        ActivationCountConditionalEffect conditional = (ActivationCountConditionalEffect)
                card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst();
        assertThat(conditional.threshold()).isEqualTo(4);
        assertThat(conditional.abilityIndex()).isEqualTo(0);
        assertThat(conditional.wrapped()).isInstanceOf(SacrificeSelfEffect.class);
    }

    @Test
    @DisplayName("Activating ability gives +1/+0")
    void activatingAbilityBoostsPower() {
        Permanent whelp = addReadyDragonWhelp(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(1);
        assertThat(whelp.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/+0")
    void canActivateMultipleTimes() {
        Permanent whelp = addReadyDragonWhelp(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(whelp.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        Permanent whelp = addReadyDragonWhelp(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        // Move to end step — conditional trigger should fire but condition not met
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // End step trigger resolves but condition not met — Dragon Whelp survives
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four or more times")
    void sacrificedWhenActivatedFourOrMoreTimes() {
        Permanent whelp = addReadyDragonWhelp(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        // Move to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // End step trigger fires and condition IS met
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated more than four times")
    void sacrificedWhenActivatedMoreThanFourTimes() {
        Permanent whelp = addReadyDragonWhelp(player1);
        harness.addMana(player1, ManaColor.RED, 5);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        // Move to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent whelp = addReadyDragonWhelp(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(0);
        assertThat(whelp.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyDragonWhelp(Player player) {
        DragonWhelp card = new DragonWhelp();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
