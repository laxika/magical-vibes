package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeleeTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap();
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    /** Puts player1 in their own declare-attackers step with priority. */
    private void enterDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }

    private void castMelee() {
        harness.setHand(player1, List.of(new Melee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    /** Advances from declare-attackers into the declare-blockers step's blocker prompt. */
    private void advanceToBlockerDeclaration() {
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Melee's controller declares the defending player's blocks")
    void controllerDeclaresBlocks() {
        enterDeclareAttackers();
        Permanent attacker = addAttacker();
        Permanent blocker = addDefenderCreature();
        castMelee();

        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("The defending player can no longer declare blocks while Melee is in force")
    void defenderCannotDeclareBlocks() {
        enterDeclareAttackers();
        addAttacker();
        addDefenderCreature();
        castMelee();

        advanceToBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unblocked attacker is untapped and removed from combat")
    void unblockedAttackerIsUntappedAndRemovedFromCombat() {
        enterDeclareAttackers();
        Permanent attacker = addAttacker();
        addDefenderCreature();
        castMelee();

        advanceToBlockerDeclaration();
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Each unblocked attacker is untapped and removed from combat")
    void eachUnblockedAttackerIsUntappedAndRemovedFromCombat() {
        enterDeclareAttackers();
        Permanent firstAttacker = addAttacker();
        Permanent secondAttacker = addAttacker();
        addDefenderCreature();
        castMelee();

        advanceToBlockerDeclaration();
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        assertThat(firstAttacker.isTapped()).isFalse();
        assertThat(firstAttacker.isAttacking()).isFalse();
        assertThat(secondAttacker.isTapped()).isFalse();
        assertThat(secondAttacker.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A blocked attacker is neither untapped nor removed from combat")
    void blockedAttackerIsUnaffected() {
        enterDeclareAttackers();
        Permanent attacker = addAttacker();
        Permanent blocker = addDefenderCreature();
        castMelee();

        advanceToBlockerDeclaration();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Both effects expire at end of combat, so the defender declares blocks again next combat")
    void effectsExpireAtEndOfCombat() {
        enterDeclareAttackers();
        Permanent attacker = addAttacker();
        addDefenderCreature();
        castMelee();

        advanceToBlockerDeclaration();
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Second combat, same setup: the defending player is prompted again.
        attacker.setAttacking(true);
        attacker.tap();
        enterDeclareAttackers();
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isFalse();

        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        // No untap-and-remove delayed trigger survived the first combat: the second combat runs to
        // its end (which clears attacking flags but never untaps), so a still-tapped attacker proves
        // the delayed ability did not fire again.
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Melee can't be cast once the declare-blockers step has begun")
    void cannotBeCastAfterBlockersStepBegins() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        addAttacker();
        harness.setHand(player1, List.of(new Melee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Melee can't be cast before combat begins")
    void cannotBeCastBeforeCombatBegins() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Melee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Melee can't be cast during an opponent's combat")
    void cannotBeCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Melee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
