package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealotIlVec.class, AshcoatBear.class, FlaringPain.class})
class ZealotIlVecTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new ZealotIlVec());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        return addCreatureReady(player2, new AshcoatBear());
    }

    private void advanceToUnblockedMay(Permanent target) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting deals 1 damage to a creature and prevents combat damage")
    void acceptDealsDamageAndPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();
        harness.setLife(player2, 20);

        advanceToUnblockedMay(victim);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The target may be a creature controlled by the attacking player")
    void canTargetOwnCreature() {
        Permanent attacker = addAttacker();
        Permanent victim = addCreatureReady(player1, new AshcoatBear());

        advanceToUnblockedMay(victim);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Declining leaves the creature undamaged and preserves combat damage")
    void declineDoesNothing() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay(victim);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("A shadow creature can block and prevents the unblocked trigger")
    void blockedAttackerDoesNotTrigger() {
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new ZealotIlVec());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Combat-damage prevention wears off at end of turn")
    void preventionWearsOff() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay(victim);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Declining allows normal combat damage to the defending player")
    void decliningAllowsCombatDamage() {
        addAttacker();
        Permanent victim = addDefenderCreature();
        harness.setLife(player2, 20);

        advanceToUnblockedMay(victim);
        harness.handleMayAbilityChosen(player1, false);
        harness.resolveCombatDamage();

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Zealot can target itself and dies from its own damage")
    void canTargetItself() {
        Permanent attacker = addAttacker();

        advanceToUnblockedMay(attacker);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Zealot il-Vec");
        harness.assertInGraveyard(player1, "Zealot il-Vec");
    }

    @Test
    @DisplayName("Unpreventable combat damage is dealt after accepting the ability")
    void unpreventableCombatDamageStillDealt() {
        addAttacker();
        Permanent victim = addDefenderCreature();
        harness.setLife(player2, 20);

        advanceToUnblockedMay(victim);
        harness.handleMayAbilityChosen(player1, true);
        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        harness.resolveCombatDamage();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }
}
