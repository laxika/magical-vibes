package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrneryTumblewagg.class, GrizzlyBears.class})
class OrneryTumblewaggTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature")
    void putsCounterAtBeginningOfCombat() {
        addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Ornery Tumblewagg")
    void saddleTapsAnotherCreature() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tumblewagg.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled doubles +1/+1 counters on a target creature")
    void attacksWhileSaddledDoublesCounters() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        tumblewagg.setSaddled(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack trigger does not happen unless Ornery Tumblewagg was saddled when it attacked")
    void doesNotDoubleCountersWhenNotSaddledAtAttack() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        tumblewagg.setSaddled(true);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
