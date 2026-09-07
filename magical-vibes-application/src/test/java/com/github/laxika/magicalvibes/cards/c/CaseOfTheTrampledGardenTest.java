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
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not solve when controlled creatures have less than 8 total power")
    void doesNotSolveWithLessThanEightTotalPower() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger the solved ability before the Case is solved")
    void doesNotTriggerBeforeSolved() {
        harness.addToBattlefield(player1, new CaseOfTheTrampledGarden());
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When solved, puts a counter on an attacking creature and gives it trample")
    void solvedAttackTriggerBoostsAttacker() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheTrampledGarden());
        Permanent first = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

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
        addReadyCreature(player1, new GrizzlyBears());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        addReadyCreature(player1, new GrizzlyBears());
        resolveEndStepTriggers();
        assertThat(casePermanent.isSolved()).isTrue();
        declareAttackers(List.of(2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
