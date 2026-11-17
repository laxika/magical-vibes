package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.FloatingCombatRules;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.testutil.Prevention;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

@CardUsed({BlindingAngel.class, Shock.class})
class BlindingAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to a player flags them to skip their next combat phase")
    void flagsPlayerOnCombatDamage() {
        Permanent angel = addCreatureReady(player1, new BlindingAngel());
        angel.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The skip lands on the damaged player, not the trigger's controller, and on no other queue")
    void flagsOnlyTheDamagedPlayersCombatPhase() {
        Permanent angel = addCreatureReady(player1, new BlindingAngel());
        angel.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.skipNextCount(player1.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(0);
        assertThat(gd.skipNextCount(null, SkipKind.TURN)).isZero();
        assertThat(gd.skipNextCount(null, SkipKind.UNTAP_STEP)).isZero();
        assertThat(gd.skipNextCount(null, SkipKind.DRAW_STEP)).isZero();
    }

    @Test
    @DisplayName("No flag when Blinding Angel is blocked and deals no damage to a player")
    void noFlagWhenBlocked() {
        Permanent angel = addCreatureReady(player1, new BlindingAngel());
        angel.setAttacking(true);

        // A second Blinding Angel (2/4 flier) can legally block and survives the 2 damage.
        Permanent blocker = addCreatureReady(player2, new BlindingAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Noncombat damage to a player does not trigger Blinding Angel")
    void noFlagForNoncombatDamage() {
        addCreatureReady(player1, new BlindingAngel());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(0);
    }

    /** Gives player2 a creature that can attack, so normal combat halts in the combat phase. */
    private void addReadyAttackerForPlayer2() {
        addCreatureReady(player2, new BlindingAngel());
    }

    @Test
    @DisplayName("Separate combat-damage triggers queue separate combat-phase skips")
    void queuesOneSkipPerTrigger() {
        Permanent firstAngel = addCreatureReady(player1, new BlindingAngel());
        Permanent secondAngel = addCreatureReady(player1, new BlindingAngel());
        firstAngel.setAttacking(true);
        secondAngel.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A flagged player jumps from precombat main straight to postcombat main")
    void skipsFlaggedCombatPhase() {
        // Without the skip, player2's ready attacker would halt progression in the combat phase.
        addReadyAttackerForPlayer2();
        FloatingCombatRules.skipNext(gd, player2.getId(), SkipKind.COMBAT_PHASE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        // The skip was consumed
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(0);
    }

    @Test
    @DisplayName("An unflagged player proceeds normally into their combat phase")
    void doesNotSkipWhenUnflagged() {
        addReadyAttackerForPlayer2();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        // Progression entered the combat phase rather than skipping to postcombat main.
        assertThat(gd.currentStep).isNotEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep.getPhaseName()).isEqualTo("Combat Phase");
    }

    @Test
    @DisplayName("A queued combat skip survives a skipped precombat main phase")
    void queuedSkipSurvivesSkippedPrecombatMainPhase() {
        FloatingCombatRules.skipNext(gd, player2.getId(), SkipKind.COMBAT_PHASE, 1);
        FloatingCombatRules.skipThisTurn(gd, player2.getId(), SkipStepOrPhaseKind.MAIN_PHASE);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isZero();
    }

    @Test
    @DisplayName("Combat damage prevention stops the combat-phase skip trigger")
    void noSkipWhenCombatDamageIsPrevented() {
        Permanent angel = addCreatureReady(player1, new BlindingAngel());
        angel.setAttacking(true);
        harness.setLife(player2, 20);
        Prevention.add(gd, PreventDamageEffect.allCombatToPlayers());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isZero();
    }

    @Test
    @DisplayName("The combat-damage trigger resolves after Blinding Angel dies")
    void triggerResolvesAfterSourceDies() {
        Permanent angel = addCreatureReady(player1, new BlindingAngel());
        angel.setAttacking(true);
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.addMana(player1, ManaColor.RED, 2);
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isZero();
        harness.castInstant(player1, 0, angel.getId());
        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Blinding Angel");
        harness.assertInGraveyard(player1, "Blinding Angel");
        harness.passBothPriorities();

        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two resolved triggers skip successive combat phases, then normal combat resumes")
    void consumesOnlyOneQueuedSkipPerCombatPhase() {
        Permanent firstAngel = addCreatureReady(player1, new BlindingAngel());
        Permanent secondAngel = addCreatureReady(player1, new BlindingAngel());
        firstAngel.setAttacking(true);
        secondAngel.setAttacking(true);
        addReadyAttackerForPlayer2();

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isEqualTo(1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCount(player2.getId(), SkipKind.COMBAT_PHASE)).isZero();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }
}
