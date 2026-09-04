package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
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

@CardUsed({ZealotIlVec.class, GrizzlyBears.class, SoltariFootSoldier.class})
class ZealotIlVecTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new ZealotIlVec());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void advanceToUnblockedMay(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
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
        Permanent blocker = new Permanent(new SoltariFootSoldier());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
}
