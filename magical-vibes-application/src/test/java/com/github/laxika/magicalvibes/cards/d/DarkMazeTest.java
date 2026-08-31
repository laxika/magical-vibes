package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnabaBodyguard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkMaze.class, AnabaBodyguard.class})
class DarkMazeTest extends BaseCardTest {

    private Permanent addMazeReady() {
        return addCreatureReady(player1, new DarkMaze());
    }

    @Test
    @DisplayName("Cannot attack without activating the ability (defender)")
    void cannotAttackWithDefender() {
        addMazeReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability lets Dark Maze attack this turn despite defender")
    void abilityAllowsAttack() {
        Permanent maze = addMazeReady();
        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new AnabaBodyguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

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

    @Test
    void activationDuringEndStepWaitsForNextEndStep() {
        addMazeReady();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }
}
