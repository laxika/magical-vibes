package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GwafaHazidProfiteerTest extends BaseCardTest {

    // ===== Activated ability: counter + controller draws =====

    @Test
    @DisplayName("Activation puts a bribery counter on the target and its controller draws a card")
    void activationPutsBriberyCounterAndControllerDraws() {
        addReadyGwafa(player1);
        Permanent bears = addBears(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities(); // resolve ability

        assertThat(bears.getCounterCount(CounterType.BRIBERY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addReadyGwafa(player1);
        Permanent ownBears = addBears(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Static: creatures with bribery counters can't attack or block =====

    @Test
    @DisplayName("A creature with a bribery counter can't attack (applies even to Gwafa's controller)")
    void briberyCounterCreatureCannotAttack() {
        addReadyGwafa(player1);
        Permanent attacker = addBears(player1);
        attacker.setCounterCount(CounterType.BRIBERY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature without a bribery counter can still attack")
    void creatureWithoutCounterCanAttack() {
        addReadyGwafa(player1);
        Permanent attacker = addBears(player2);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player2, List.of(index));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature with a bribery counter can't block")
    void briberyCounterCreatureCannotBlock() {
        addReadyGwafa(player1);
        Permanent attacker = addBears(player1);
        attacker.setAttacking(true);

        Permanent blocker = addBears(player2);
        blocker.setCounterCount(CounterType.BRIBERY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Gwafa at attacker-battlefield index 0; the attacking Bears is at index 1.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyGwafa(Player player) {
        Permanent perm = new Permanent(new GwafaHazidProfiteer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBears(Player controller) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }
}
