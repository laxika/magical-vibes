package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AirNomadStudent.class)
class AirNomadStudentTest extends BaseCardTest {

    private Permanent addStudent() {
        Permanent student = harness.addToBattlefieldAndReturn(player1, new AirNomadStudent());
        student.setSummoningSick(false);
        return student;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at its controller's end step if it did not attack")
    void putsCounterWhenItDidNotAttack() {
        Permanent student = addStudent();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(student.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself when it attacked this turn")
    void doesNotPutCounterWhenItAttacked() {
        Permanent student = addStudent();
        student.setAttackedThisTurn(true);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(student.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void triggersOnlyDuringItsControllersEndStep() {
        Permanent student = addStudent();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(student.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Student still gets its end-step counter")
    void putsCounterOnStudentThatEnteredThisTurn() {
        Permanent student = harness.enterBattlefieldAndReturn(player1, new AirNomadStudent());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(student.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Student checks its own attack history and puts counters only on itself")
    void checksEachStudentsAttackHistorySeparately() {
        Permanent firstStudent = addStudent();
        Permanent secondStudent = addStudent();
        Permanent attackingStudent = addStudent();
        attackingStudent.setAttackedThisTurn(true);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstStudent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondStudent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attackingStudent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Student entering after the end step begins does not get another Student's counter")
    void studentEnteringDuringEndStepDoesNotTrigger() {
        Permanent originalStudent = addStudent();
        advanceToEndStep(player1);

        Permanent lateStudent = harness.enterBattlefieldAndReturn(player1, new AirNomadStudent());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(originalStudent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateStudent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
