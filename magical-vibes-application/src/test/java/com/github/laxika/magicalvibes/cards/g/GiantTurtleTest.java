package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantTurtle.class})
class GiantTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Can't attack if it attacked during its controller's last turn")
    void cannotAttackAfterAttackingLastTurn() {
        Permanent turtle = addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        turtle.setAttacking(true);
        turtle.clearCombatState();
        advanceTurn();
        advanceTurn();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);
    }

    @Test
    @DisplayName("May attack if it did not attack during its controller's last turn")
    void canAttackAfterNotAttackingLastTurn() {
        addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("The restriction applies only to the Giant Turtle that attacked")
    void restrictionIsSpecificToEachTurtle() {
        Permanent attacker = addCreatureReady(player1, new GiantTurtle());
        addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        attacker.setAttacking(true);
        attacker.clearCombatState();
        advanceTurn();
        advanceTurn();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(1)
                .doesNotContain(0);
    }

    @Test
    @DisplayName("The restriction lifts after a turn spent not attacking")
    void restrictionLiftsAfterIdleTurn() {
        Permanent turtle = addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        turtle.setAttacking(true);
        turtle.clearCombatState();
        advanceTurn();
        advanceTurn();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);

        advanceTurn();
        advanceTurn();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
