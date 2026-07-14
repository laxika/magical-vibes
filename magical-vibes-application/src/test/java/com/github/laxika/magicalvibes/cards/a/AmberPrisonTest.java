package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmberPrisonTest extends BaseCardTest {

    // ===== Activated ability: tap target artifact / creature / land =====

    @Test
    @DisplayName("Resolving ability taps target artifact")
    void resolvingTapsTargetArtifact() {
        addReadyAmberPrison(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyAmberPrison(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target land")
    void resolvingTapsTargetLand() {
        addReadyAmberPrison(player1);
        Permanent target = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Amber Prison itself")
    void activatingTapsAmberPrison() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(amberPrison.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent that is not an artifact, creature, or land")
    void cannotTargetEnchantment() {
        addReadyAmberPrison(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== Prevent untap while source tapped =====

    @Test
    @DisplayName("Locked permanent does not untap while Amber Prison is tapped")
    void lockedPermanentDoesNotUntap() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(amberPrison.isTapped()).isTrue();

        // Advance to player2's turn — their creature should NOT untap
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Locked permanent untaps after Amber Prison is untapped")
    void lockedPermanentUntapsWhenSourceUntaps() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // player1's turn — choose to UNTAP Amber Prison
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(amberPrison.isTapped()).isFalse();

        // player2's turn — their creature should now untap (lock cleared)
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locked permanent untaps when Amber Prison leaves the battlefield")
    void lockedPermanentUntapsWhenSourceRemoved() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(amberPrison);

        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    // ===== May not untap during untap step =====

    @Test
    @DisplayName("Choosing to untap Amber Prison actually untaps it")
    void choosingToUntapWorks() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        amberPrison.tap();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(amberPrison.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing NOT to untap Amber Prison keeps it tapped")
    void choosingNotToUntapKeepsTapped() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        amberPrison.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(amberPrison.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Keeping Amber Prison tapped maintains lock across multiple turns")
    void keepingTappedMaintainsLockAcrossTurns() {
        Permanent amberPrison = addReadyAmberPrison(player1);
        Permanent target = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // player2's turn — land stays tapped
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isTrue();

        // player1's turn — choose NOT to untap Amber Prison
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(amberPrison.isTapped()).isTrue();

        // player2's turn — land STILL stays tapped
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyAmberPrison(Player player) {
        return addReady(player, new Permanent(new AmberPrison()));
    }

    private Permanent addReadyArtifact(Player player) {
        return addReady(player, new Permanent(new AngelsFeather()));
    }

    private Permanent addReadyCreature(Player player) {
        return addReady(player, new Permanent(new GrizzlyBears()));
    }

    private Permanent addReadyLand(Player player) {
        return addReady(player, new Permanent(new Forest()));
    }

    private Permanent addReadyEnchantment(Player player) {
        return addReady(player, new Permanent(new Pacifism()));
    }

    private Permanent addReady(Player player, Permanent perm) {
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
