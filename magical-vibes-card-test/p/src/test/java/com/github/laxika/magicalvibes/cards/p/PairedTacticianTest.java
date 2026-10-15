package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarriorEnKor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PairedTactician.class, WarriorEnKor.class, GrizzlyBears.class, AmoeboidChangeling.class})
class PairedTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when it attacks with another Warrior")
    void putsCounterWhenAttackingWithAnotherWarrior() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new WarriorEnKor());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without another attacking Warrior")
    void doesNotTriggerWithoutAnotherAttackingWarrior() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenAttackingAlone() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenOtherWarriorStaysBehind() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new PairedTactician());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenTacticianDoesNotAttack() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new PairedTactician());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsOnlyOneCounterWithMultipleOtherWarriors() {
        Permanent first = addCreatureReady(player1, new PairedTactician());
        Permanent second = addCreatureReady(player1, new PairedTactician());
        Permanent third = addCreatureReady(player1, new PairedTactician());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(AmoeboidChangeling.class)
    void triggersWithAnotherWarriorEvenAfterLosingItsOwnCreatureTypes() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        Permanent otherWarrior = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new AmoeboidChangeling());

        harness.activateAbility(player1, 2, 1, null, tactician.getId());
        resolveAllTriggers();
        assertThat(gqs.effectiveCreatureSubtypes(gd, tactician)).doesNotContain(CardSubtype.WARRIOR);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherWarrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed(AmoeboidChangeling.class)
    void stillGetsCounterWhenOtherAttackerLosesWarriorTypeAfterTriggering() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        Permanent otherWarrior = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new AmoeboidChangeling());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(gd.stack).hasSize(2);

            harness.activateAbility(player1, 2, 1, null, otherWarrior.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.effectiveCreatureSubtypes(gd, otherWarrior)).doesNotContain(CardSubtype.WARRIOR);
        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherWarrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
