package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShuEliteInfantryTest extends BaseCardTest {

    // ===== Can't attack alone =====

    @Test
    @DisplayName("Shu Elite Infantry can't attack alone")
    void cantAttackAlone() {
        Permanent infantry = new Permanent(new ShuEliteInfantry());
        infantry.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(infantry);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shu Elite Infantry can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        Permanent infantry = new Permanent(new ShuEliteInfantry());
        infantry.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(infantry);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));

        // Shu Elite Infantry (3/3) + Grizzly Bears (2/2) = 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Can't block alone =====

    @Test
    @DisplayName("Shu Elite Infantry can't block alone")
    void cantBlockAlone() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent infantry = new Permanent(new ShuEliteInfantry());
        infantry.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(infantry);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shu Elite Infantry can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = new Permanent(new GrizzlyBears());
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker1);

        Permanent attacker2 = new Permanent(new GrizzlyBears());
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker2);

        Permanent infantry = new Permanent(new ShuEliteInfantry());
        infantry.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(infantry);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(infantry.isBlocking()).isTrue();
        assertThat(bears.isBlocking()).isTrue();
    }
}
