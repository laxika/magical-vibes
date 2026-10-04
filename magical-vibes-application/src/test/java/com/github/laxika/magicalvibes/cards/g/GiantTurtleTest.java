package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RubiniaSoulsinger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantTurtle.class, RubiniaSoulsinger.class})
class GiantTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Can't attack if it attacked during its controller's last turn")
    void cannotAttackAfterAttackingLastTurn() {
        Permanent turtle = addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        turtle.setAttacking(true);
        turtle.clearCombatState();
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);
    }

    @Test
    @DisplayName("May attack if it did not attack during its controller's last turn")
    void canAttackAfterNotAttackingLastTurn() {
        addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);

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
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(1)
                .doesNotContain(0);
    }

    @Test
    @DisplayName("An opponent's attack does not count as this Turtle's controller's last-turn attack")
    void opponentAttackDoesNotCreateRestriction() {
        addCreatureReady(player1, new GiantTurtle());
        Permanent opponentTurtle = addCreatureReady(player2, new GiantTurtle());

        harness.forceActivePlayer(player1);
        advanceToNextUpkeep(player2);
        opponentTurtle.setAttacking(true);
        opponentTurtle.clearCombatState();
        advanceToNextUpkeep(player1);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("The restriction lifts after a turn spent not attacking")
    void restrictionLiftsAfterIdleTurn() {
        Permanent turtle = addCreatureReady(player1, new GiantTurtle());

        harness.forceActivePlayer(player1);
        turtle.setAttacking(true);
        turtle.clearCombatState();
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);

        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("An attack this turn does not prevent attacking in another combat this turn")
    void canAttackAgainDuringTheSameTurn() {
        Permanent turtle = addCreatureReady(player1, new GiantTurtle());
        harness.forceActivePlayer(player1);
        turtle.setAttacking(true);
        turtle.clearCombatState();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Taking a Turtle after an opponent's attack does not restrict its new controller's next turn")
    void canAttackAfterGainingControlDuringOpponentsTurn() {
        Permanent turtle = addCreatureReady(player1, new GiantTurtle());
        addCreatureReady(player2, new RubiniaSoulsinger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        turtle.setAttacking(true);
        turtle.clearCombatState();

        harness.activateAbility(player2, 0, null, turtle.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(turtle);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(player2, false);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId())).contains(1);
    }

    private void advanceToNextUpkeep(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }
}
