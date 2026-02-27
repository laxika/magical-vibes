package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetUntapWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RustTickTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Rust Tick has MayNotUntapDuringUntapStepEffect as static effect")
    void hasStaticMayNotUntapEffect() {
        RustTick card = new RustTick();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MayNotUntapDuringUntapStepEffect.class);
    }

    @Test
    @DisplayName("Rust Tick has activated ability with tap and prevent untap effects")
    void hasActivatedAbilityWithCorrectEffects() {
        RustTick card = new RustTick();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(TapTargetPermanentEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(1))
                .isInstanceOf(PreventTargetUntapWhileSourceTappedEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter =
                (PermanentPredicateTargetFilter) card.getActivatedAbilities().get(0).getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Activated ability: tap target artifact =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an artifact")
    void activatingPutsOnStack() {
        Permanent rustTick = addReadyRustTick(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetArtifact.getId());
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
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    // ===== Prevent untap while source tapped =====

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

    // ===== May not untap during untap step =====

    @Test
    @DisplayName("Controller is prompted whether to untap tapped Rust Tick during untap step")
    void controllerIsPromptedToUntapRustTick() {
        Permanent rustTick = addReadyRustTick(player1);
        rustTick.tap();

        // Advance to player1's turn — should be prompted for may-not-untap
        // The auto-pass cascade from END_STEP goes through CLEANUP into advanceTurn,
        // which sets up the may ability prompt and stops.
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // Cascades: END_STEP -> CLEANUP -> advanceTurn -> may ability prompt

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

    // ===== Helpers =====

    private Permanent addReadyRustTick(Player player) {
        Permanent perm = new Permanent(new RustTick());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent perm = new Permanent(new AngelsFeather());
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

    /**
     * Advance turn from the given active player to the next player's turn.
     * Use when the next active player does NOT have may-not-untap permanents.
     */
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

    /**
     * Advance turn from the given active player to the next player's turn,
     * handling a may-not-untap prompt for the next player.
     *
     * @param currentActivePlayer the player whose turn is ending
     * @param acceptUntap true to untap the permanent, false to keep it tapped
     */
    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // Cascades: END_STEP -> CLEANUP -> advanceTurn -> may ability prompt

        // Determine which player is the new active player (the one receiving the prompt)
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
