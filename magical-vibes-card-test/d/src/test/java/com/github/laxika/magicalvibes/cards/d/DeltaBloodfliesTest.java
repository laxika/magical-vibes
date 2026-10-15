package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeltaBloodflies.class, Swamp.class})
class DeltaBloodfliesTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes each opponent lose 1 life when you control a creature with a counter")
    void attackCausesLifeLossWithCounter() {
        addCreatureReady(player1, new DeltaBloodflies());
        Permanent otherCreature = addCreatureReady(player1, new DeltaBloodflies());
        otherCreature.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Attacking does not cause life loss without a creature with a counter")
    void attackDoesNotCauseLifeLossWithoutCounter() {
        addCreatureReady(player1, new DeltaBloodflies());
        addCreatureReady(player1, new DeltaBloodflies());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    void counterOnAttackerItselfQualifies() {
        Permanent attacker = addCreatureReady(player1, new DeltaBloodflies());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();
            harness.assertLife(player2, 19);
            harness.assertLife(player1, 20);
        });
    }

    @Test
    void opponentsCounterDoesNotQualify() {
        addCreatureReady(player1, new DeltaBloodflies());
        Permanent opponentCreature = addCreatureReady(player2, new DeltaBloodflies());
        opponentCreature.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).isEmpty();
            harness.assertLife(player2, 20);
        });
    }

    @Test
    void counterOnNoncreatureDoesNotQualify() {
        addCreatureReady(player1, new DeltaBloodflies());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        land.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).isEmpty();
            harness.assertLife(player2, 20);
        });
    }

    @Test
    void removingLastCounterBeforeResolutionPreventsLifeLoss() {
        Permanent attacker = addCreatureReady(player1, new DeltaBloodflies());
        attacker.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            attacker.setCounterCount(CounterType.CHARGE, 0);
            resolveAllTriggers();
            harness.assertLife(player2, 20);
        });
    }

    @Test
    void addingCounterAfterAttackDoesNotCreateTrigger() {
        Permanent attacker = addCreatureReady(player1, new DeltaBloodflies());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).isEmpty();
            attacker.setCounterCount(CounterType.CHARGE, 1);
            resolveAllTriggers();
            harness.assertLife(player2, 20);
        });
    }

    @Test
    void differentCreatureCanSatisfyConditionAtResolution() {
        Permanent attacker = addCreatureReady(player1, new DeltaBloodflies());
        Permanent otherCreature = addCreatureReady(player1, new DeltaBloodflies());
        attacker.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            attacker.setCounterCount(CounterType.CHARGE, 0);
            otherCreature.setCounterCount(CounterType.STUN, 1);
            resolveAllTriggers();
            harness.assertLife(player2, 19);
        });
    }
}
