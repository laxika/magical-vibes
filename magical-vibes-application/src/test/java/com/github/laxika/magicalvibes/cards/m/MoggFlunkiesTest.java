package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MoggFlunkies.class)
class MoggFlunkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Mogg Flunkies can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new MoggFlunkies());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mogg Flunkies can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MoggFlunkies());
        addCreatureReady(player1, new MoggFlunkies());

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Mogg Flunkies can't block alone")
    void cantBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new MoggFlunkies());
        attacker.setAttacking(true);

        addCreatureReady(player2, new MoggFlunkies());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mogg Flunkies can't attack alone when another creature does not attack")
    void cantAttackAloneWhenAnotherCreatureDoesNotAttack() {
        addCreatureReady(player1, new MoggFlunkies());
        addCreatureReady(player1, new MoggFlunkies());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mogg Flunkies can't block alone when another creature does not block")
    void cantBlockAloneWhenAnotherCreatureDoesNotBlock() {
        Permanent attacker = addCreatureReady(player1, new MoggFlunkies());
        attacker.setAttacking(true);

        addCreatureReady(player2, new MoggFlunkies());
        addCreatureReady(player2, new MoggFlunkies());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mogg Flunkies can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = addCreatureReady(player1, new MoggFlunkies());
        attacker1.setAttacking(true);

        Permanent attacker2 = addCreatureReady(player1, new MoggFlunkies());
        attacker2.setAttacking(true);

        Permanent flunkies = addCreatureReady(player2, new MoggFlunkies());

        Permanent secondBlocker = addCreatureReady(player2, new MoggFlunkies());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(flunkies.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
