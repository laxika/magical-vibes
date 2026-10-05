package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({LeatherheadIronGator.class, GrizzlyBears.class, Forest.class})
class LeatherheadIronGatorTest extends BaseCardTest {

    @Test
    @DisplayName("When Leatherhead attacks, it puts two +1/+1 counters on each creature you control")
    void putsCountersOnEachCreatureYouControl() {
        Permanent leatherhead = addCreatureReady(player1, new LeatherheadIronGator());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(leatherhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Leatherhead's ability does not trigger when another creature attacks")
    void doesNotTriggerWhenAnotherCreatureAttacks() {
        Permanent leatherhead = addCreatureReady(player1, new LeatherheadIronGator());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(leatherhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creatures entering after the attack receive counters when the trigger resolves")
    void includesCreaturesEnteringBeforeResolution() {
        Permanent leatherhead = addCreatureReady(player1, new LeatherheadIronGator());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(leatherhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveAllTriggers();

        assertThat(leatherhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(newCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger still puts counters on creatures after Leatherhead leaves")
    void resolvesAfterLeatherheadLeavesBattlefield() {
        Permanent leatherhead = addCreatureReady(player1, new LeatherheadIronGator());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(leatherhead);
        gd.playerGraveyards.get(player1.getId()).add(leatherhead.getCard());

        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
