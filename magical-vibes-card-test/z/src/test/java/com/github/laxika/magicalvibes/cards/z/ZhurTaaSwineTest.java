package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhurTaaSwine.class})
class ZhurTaaSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodrush gives target attacking creature +5/+4 until end of turn")
    void bloodrushBoostsAttackingCreature() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.assertNotInHand(player1, "Zhur-Taa Swine");
        harness.assertInGraveyard(player1, "Zhur-Taa Swine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(8);
        harness.assertInGraveyard(player1, "Zhur-Taa Swine");
    }

    @Test
    @DisplayName("The bloodrush boost wears off at end of turn")
    void bloodrushBoostWearsOff() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bloodrush cannot target a creature that isn't attacking")
    void bloodrushRejectsNonAttackingCreature() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Zhur-Taa Swine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodrush can boost an opponent's attacking creature")
    void bloodrushCanTargetOpponentsAttacker() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player2, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(8);
        harness.assertInGraveyard(player1, "Zhur-Taa Swine");
    }

    @Test
    @DisplayName("Bloodrush does not resolve if its target stops attacking")
    void bloodrushRechecksAttackingTargetOnResolution() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Zhur-Taa Swine");
        harness.assertNotInHand(player1, "Zhur-Taa Swine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodrush cannot be activated without enough mana")
    void bloodrushRejectsInsufficientMana() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Zhur-Taa Swine");
        harness.assertNotInGraveyard(player1, "Zhur-Taa Swine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodrush requires red mana even when three mana are available")
    void bloodrushRejectsMissingRequiredColor() {
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Zhur-Taa Swine");
        harness.assertNotInGraveyard(player1, "Zhur-Taa Swine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
