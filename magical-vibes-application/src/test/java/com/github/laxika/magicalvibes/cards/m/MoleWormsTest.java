package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoleWormsTest extends BaseCardTest {

    // ===== Activated ability: tap target land =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingPutsOnStack() {
        addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(targetLand.getId());
    }

    @Test
    @DisplayName("Resolving ability taps target land")
    void resolvingTapsTargetLand() {
        addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Mole Worms itself")
    void activatingTapsMoleWorms() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());

        assertThat(moleWorms.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target non-land permanents")
    void cannotTargetNonLand() {
        addReadyMoleWorms(player1);
        Permanent creature = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Prevent untap while source tapped =====

    @Test
    @DisplayName("Locked land does not untap during controller's untap step while Mole Worms is tapped")
    void lockedLandDoesNotUntap() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
        assertThat(moleWorms.isTapped()).isTrue();

        // Advance to player2's turn — their land should NOT untap
        advanceToNextTurn(player1);

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Locked land untaps after Mole Worms is untapped")
    void lockedLandUntapsWhenMoleWormsUntaps() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
        assertThat(moleWorms.isTapped()).isTrue();

        // player1's turn — Mole Worms has may-not-untap; choose to UNTAP it
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(moleWorms.isTapped()).isFalse();

        // player2's turn — their land should now untap (lock cleared)
        advanceToNextTurn(player1);

        assertThat(targetLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locked land untaps when Mole Worms leaves the battlefield")
    void lockedLandUntapsWhenMoleWormsRemoved() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(moleWorms);

        // player2's turn — land should untap (source gone)
        advanceToNextTurn(player1);

        assertThat(targetLand.isTapped()).isFalse();
    }

    // ===== May not untap during untap step =====

    @Test
    @DisplayName("Choosing to untap Mole Worms actually untaps it")
    void choosingToUntapWorks() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        moleWorms.tap();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(moleWorms.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing NOT to untap Mole Worms keeps it tapped")
    void choosingNotToUntapKeepsTapped() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        moleWorms.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(moleWorms.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Keeping Mole Worms tapped maintains lock on target land across multiple turns")
    void keepingTappedMaintainsLockAcrossTurns() {
        Permanent moleWorms = addReadyMoleWorms(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        // player2's turn — land stays tapped
        advanceToNextTurn(player1);
        assertThat(targetLand.isTapped()).isTrue();

        // player1's turn — choose NOT to untap Mole Worms
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(moleWorms.isTapped()).isTrue();

        // player2's turn — land STILL stays tapped
        advanceToNextTurn(player1);
        assertThat(targetLand.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyMoleWorms(Player player) {
        Permanent perm = new Permanent(new MoleWorms());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // Cascades: END_STEP -> CLEANUP -> advanceTurn -> may ability prompt

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
