package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiparianTiger.class})
class RiparianTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new RiparianTiger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay energy on attack to get +2/+2 until end of turn")
    void paysEnergyOnAttack() {
        Permanent tiger = addCreatureReady(player1, new RiparianTiger());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, tiger)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attack boost expires at end of turn")
    void attackBoostExpiresAtEndOfTurn() {
        Permanent tiger = addCreatureReady(player1, new RiparianTiger());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tiger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot pay the attack cost without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        Permanent tiger = addCreatureReady(player1, new RiparianTiger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tiger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining payment keeps energy and does not boost the attacker")
    void decliningPaymentKeepsEnergy() {
        Permanent tiger = addCreatureReady(player1, new RiparianTiger());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tiger)).isEqualTo(4);
    }

    @Test
    @DisplayName("One energy cannot be partially paid for the attack boost")
    void cannotPartiallyPayAttackCost() {
        Permanent tiger = addCreatureReady(player1, new RiparianTiger());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tiger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Payment spends exactly two energy and boosts only the attacking Tiger")
    void spendsExactlyTwoEnergyAndBoostsOnlyAttacker() {
        Permanent attacker = addCreatureReady(player1, new RiparianTiger());
        Permanent otherTiger = addCreatureReady(player1, new RiparianTiger());
        gd.playerEnergyCounters.put(player1.getId(), 5);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherTiger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherTiger)).isEqualTo(4);
    }
}
