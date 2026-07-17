package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkMazeTest extends BaseCardTest {

    private Permanent addMazeReady() {
        DarkMaze card = new DarkMaze();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }

    @Test
    @DisplayName("Cannot attack without activating the ability (defender)")
    void cannotAttackWithDefender() {
        addMazeReady();

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability lets Dark Maze attack this turn despite defender")
    void abilityAllowsAttack() {
        Permanent maze = addMazeReady();
        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(maze.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Dark Maze is exiled at the beginning of the next end step after activating")
    void exiledAtNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        addMazeReady();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Still on the battlefield during the main phase.
        harness.assertOnBattlefield(player1, "Dark Maze");

        // Advance to the end step — Dark Maze should be exiled.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dark Maze");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dark Maze"));
    }
}
