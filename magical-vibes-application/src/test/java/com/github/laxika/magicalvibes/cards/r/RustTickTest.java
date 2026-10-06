package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustTick.class, Memnite.class, CarapaceForger.class})
class RustTickTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an artifact")
    void activatingPutsOnStack() {
        addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(targetArtifact.getId());
    }

    @Test
    @DisplayName("Resolving ability taps target artifact")
    void resolvingTapsTargetArtifact() {
        addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Rust Tick itself")
    void activatingTapsRustTick() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());

        assertThat(rustTick.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target non-artifact permanents")
    void cannotTargetNonArtifact() {
        addReadyRustTick(player1);
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Locked artifact does not untap during controller's untap step while Rust Tick is tapped")
    void lockedArtifactDoesNotUntap() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate ability to tap and lock the artifact
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
        assertThat(rustTick.isTapped()).isTrue();

        // Advance to player2's turn — their artifact should NOT untap
        advanceToNextTurn(player1);

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Locked artifact untaps after Rust Tick is untapped")
    void lockedArtifactUntapsWhenRustTickUntaps() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate ability to tap and lock the artifact
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
        assertThat(rustTick.isTapped()).isTrue();

        // Advance to player1's turn — Rust Tick controller's turn
        // Rust Tick has may-not-untap; choose to UNTAP it
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(rustTick.isTapped()).isFalse();

        // Advance to player2's turn — their artifact should now untap (lock cleared)
        advanceToNextTurn(player1);

        assertThat(targetArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locked artifact untaps when Rust Tick leaves the battlefield")
    void lockedArtifactUntapsWhenRustTickRemoved() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate ability to tap and lock the artifact
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();

        // Remove Rust Tick from the battlefield
        gd.playerBattlefields.get(player1.getId()).remove(rustTick);

        // Advance to player2's turn — artifact should untap (source gone)
        advanceToNextTurn(player1);

        assertThat(targetArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller is prompted whether to untap tapped Rust Tick during untap step")
    void controllerIsPromptedToUntapRustTick() {
        Permanent rustTick = addReadyRustTick(player1);
        rustTick.tap();

        harness.performUntapStep(player1);

        // Game should be awaiting may ability choice
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Choosing to untap Rust Tick actually untaps it")
    void choosingToUntapWorks() {
        Permanent rustTick = addReadyRustTick(player1);
        rustTick.tap();

        // Advance to player1's turn and choose to untap
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(rustTick.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing NOT to untap Rust Tick keeps it tapped")
    void choosingNotToUntapKeepsTapped() {
        Permanent rustTick = addReadyRustTick(player1);
        rustTick.tap();

        // Advance to player1's turn and choose NOT to untap
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(rustTick.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped Rust Tick is not prompted during untap step")
    void untappedRustTickNotPrompted() {
        Permanent rustTick = addReadyRustTick(player1);
        // Rust Tick is untapped — should not be prompted

        // Advance to player1's turn — no may ability prompt expected
        advanceToNextTurn(player2);

        // If we got here without needing a may-ability choice, the test passes
        assertThat(rustTick.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Keeping Rust Tick tapped maintains lock on target artifact across multiple turns")
    void keepingTappedMaintainsLockAcrossTurns() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Lock the artifact
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        // Turn 1: player2's turn — artifact stays tapped
        advanceToNextTurn(player1);
        assertThat(targetArtifact.isTapped()).isTrue();

        // Turn 2: player1's turn — choose NOT to untap Rust Tick
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(rustTick.isTapped()).isTrue();

        // Turn 3: player2's turn — artifact STILL stays tapped
        advanceToNextTurn(player1);
        assertThat(targetArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped artifact can be targeted and locked")
    void alreadyTappedArtifactIsLocked() {
        addReadyRustTick(player1);
        Permanent artifact = addReadyArtifact(player2);
        artifact.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability still taps its target when Rust Tick leaves before resolution")
    void sourceRemovedBeforeResolutionStillTapsWithoutLock() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent artifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rustTick);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untapping and retapping Rust Tick before resolution prevents the lock")
    void sourceUntappedAndRetappedBeforeResolutionDoesNotLock() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent artifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        rustTick.untap();
        rustTick.tap();
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Independently untapping the target does not end Rust Tick's lock")
    void independentlyUntappedTargetRemainsLockedWhenRetapped() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent artifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        artifact.untap();
        assertThat(artifact.isTapped()).isFalse();
        artifact.tap();
        harness.performUntapStep(player2);

        assertThat(rustTick.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A released lock does not resume when Rust Tick is tapped again")
    void releasedLockDoesNotResumeWhenSourceIsRetapped() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent artifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        rustTick.tap();
        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isFalse();
    }

    private Permanent addReadyRustTick(Player player) {
        return addCreatureReady(player, new RustTick());
    }

    private Permanent addReadyArtifact(Player player) {
        return addCreatureReady(player, new Memnite());
    }

    /**
     * Perform the next player's untap step after the given active player.
     * Use when the next active player does NOT have may-not-untap permanents.
     */
    private void advanceToNextTurn(Player currentActivePlayer) {
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.performUntapStep(nextActivePlayer);
    }

    /**
     * Perform the next player's untap step after the given active player,
     * handling a may-not-untap prompt for the next player.
     *
     * @param currentActivePlayer the player whose turn is ending
     * @param acceptUntap true to untap the permanent, false to keep it tapped
     */
    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.performUntapStep(newActivePlayer);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
