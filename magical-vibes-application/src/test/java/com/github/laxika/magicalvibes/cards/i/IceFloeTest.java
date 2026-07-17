package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IceFloeTest extends BaseCardTest {

    // ===== Activated ability: tap target attacking creature =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting the attacker")
    void activatingPutsOnStack() {
        addReadyIceFloe(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving ability taps the attacking creature")
    void resolvingTapsAttacker() {
        addReadyIceFloe(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Ice Floe itself")
    void activatingTapsIceFloe() {
        Permanent iceFloe = addReadyIceFloe(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(iceFloe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        addReadyIceFloe(player1);
        Permanent flyer = addAttacker(player2, player1, new SuntailHawk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking you")
    void cannotTargetNonAttacker() {
        addReadyIceFloe(player1);
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Prevent untap while Ice Floe stays tapped =====

    @Test
    @DisplayName("Locked creature does not untap during its controller's untap step while Ice Floe is tapped")
    void lockedCreatureDoesNotUntap() {
        addReadyIceFloe(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.isTapped()).isTrue();

        // player2 -> player1: keep Ice Floe tapped (choose NOT to untap)
        advanceToNextTurnWithMayChoice(player2, false);
        // player1 -> player2: attacker's untap step, still locked
        advanceToNextTurn(player1);

        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Locked creature untaps once Ice Floe untaps")
    void lockedCreatureUntapsWhenIceFloeUntaps() {
        Permanent iceFloe = addReadyIceFloe(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.isTapped()).isTrue();

        // player2 -> player1: untap Ice Floe (releases the lock)
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(iceFloe.isTapped()).isFalse();

        // player1 -> player2: attacker now untaps
        advanceToNextTurn(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    // ===== May not untap during untap step =====

    @Test
    @DisplayName("Choosing NOT to untap Ice Floe keeps it tapped")
    void choosingNotToUntapKeepsTapped() {
        Permanent iceFloe = addReadyIceFloe(player1);
        iceFloe.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(iceFloe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing to untap Ice Floe untaps it")
    void choosingToUntapWorks() {
        Permanent iceFloe = addReadyIceFloe(player1);
        iceFloe.tap();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(iceFloe.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyIceFloe(Player player) {
        Permanent perm = new Permanent(new IceFloe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
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
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // Cascades into advanceTurn -> may ability prompt

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
