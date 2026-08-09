package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LaccolithTitanTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new LaccolithTitan());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting deals power damage to a target creature and prevents combat damage")
    void acceptDealsPowerDamageAndPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Declining deals no damage and does not prevent combat damage")
    void declineDoesNothing() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("An unblocked attacker does not trigger")
    void unblockedDoesNotTrigger() {
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Combat-damage prevention wears off at end of turn")
    void preventionWearsOff() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }
}
