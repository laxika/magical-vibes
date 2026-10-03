package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheTrampledGarden.class, GrizzlyBears.class})
class CaseOfTheTrampledGardenTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, distributes two +1/+1 counters among one or two creatures you control")
    void distributesCountersWhenItEnters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Solves at the beginning of the end step when controlled creatures have total power 8")
    void solvesWithEightTotalPower() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not solve when controlled creatures have less than 8 total power")
    void doesNotSolveWithLessThanEightTotalPower() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger the solved ability before the Case is solved")
    void doesNotTriggerBeforeSolved() {
        harness.addToBattlefield(player1, new CaseOfTheTrampledGarden());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When solved, puts a counter on an attacking creature and gives it trample")
    void solvedAttackTriggerBoostsAttacker() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        resolveEndStepTriggers();
        assertThat(casePermanent.isSolved()).isTrue();
        declareAttackers(List.of(1, 4));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The attack trigger cannot target a nonattacking creature")
    void attackTriggerCannotTargetNonattacker() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        addCreatureReady(player1, new GrizzlyBears());
        resolveEndStepTriggers();
        assertThat(casePermanent.isSolved()).isTrue();
        declareAttackers(List.of(2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
    }

    @Test
    void distributesOneCounterToEachOfTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotRedistributeCountersWhenOneTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotCountOpponentsCreaturesTowardSolving() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void doesNotSolveOnOpponentsEndStep() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(casePermanent.isSolved()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksTotalPowerWhenSolveTriggerResolves() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
        Permanent fourth = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(fourth);
        gd.playerGraveyards.get(player1.getId()).add(fourth.getCard());
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void solvedAttackAbilityResolvesAfterCaseLeaves() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
        resolveEndStepTriggers();
        assertThat(casePermanent.isSolved()).isTrue();
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(casePermanent);
        gd.playerGraveyards.get(player1.getId()).add(casePermanent.getCard());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void countersContributeToSolvingAndTrampleExpiresButCounterRemains() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent casePermanent = harness.enterBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        resolveEndStepTriggers();
        assertThat(casePermanent.isSolved()).isTrue();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(casePermanent.isSolved()).isTrue();
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
