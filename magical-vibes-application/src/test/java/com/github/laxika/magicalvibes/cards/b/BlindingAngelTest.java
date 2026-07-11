package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlindingAngelTest extends BaseCardTest {

    private Permanent addReadyCreature(Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Dealing combat damage to a player flags them to skip their next combat phase")
    void flagsPlayerOnCombatDamage() {
        Permanent angel = addReadyCreature(new BlindingAngel());
        angel.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("No flag when Blinding Angel is blocked and deals no damage to a player")
    void noFlagWhenBlocked() {
        Permanent angel = addReadyCreature(new BlindingAngel());
        angel.setAttacking(true);

        // Serra Angel (4/4 flier) can legally block and survives the 2 damage
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    /** Gives player2 a creature that can attack, so normal combat halts in the combat phase. */
    private void addReadyAttackerForPlayer2() {
        Permanent bear = new Permanent(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear);
    }

    @Test
    @DisplayName("A flagged player jumps from precombat main straight to postcombat main")
    void skipsFlaggedCombatPhase() {
        // Without the skip, player2's ready attacker would halt progression in the combat phase.
        addReadyAttackerForPlayer2();
        gd.skipNextCombatPhaseCount.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        // The skip was consumed
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("An unflagged player proceeds normally into their combat phase")
    void doesNotSkipWhenUnflagged() {
        addReadyAttackerForPlayer2();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Progression entered the combat phase rather than skipping to postcombat main.
        assertThat(gd.currentStep).isNotEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep.getPhaseName()).isEqualTo("Combat Phase");
    }
}
