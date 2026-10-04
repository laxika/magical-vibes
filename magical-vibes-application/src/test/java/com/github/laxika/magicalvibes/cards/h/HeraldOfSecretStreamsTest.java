package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;
import java.util.List;

@CardUsed({HeraldOfSecretStreams.class, GrizzlyBears.class})
class HeraldOfSecretStreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Creature with +1/+1 counter can't be blocked")
    void creatureWithCounterCantBeBlocked() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Creature without +1/+1 counter can still be blocked")
    void creatureWithoutCounterCanBeBlocked() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Herald itself can't be blocked if it has +1/+1 counters")
    void heraldWithCounterCantBeBlocked() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfSecretStreams());

        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasCantBeBlocked(gd, herald)).isTrue();
    }

    @Test
    @DisplayName("Herald without counters can be blocked")
    void heraldWithoutCounterCanBeBlocked() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfSecretStreams());

        assertThat(gqs.hasCantBeBlocked(gd, herald)).isFalse();
    }

    @Test
    @DisplayName("Does not affect opponent's creature with +1/+1 counter")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        opponentBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasCantBeBlocked(gd, opponentBears)).isFalse();
    }

    @Test
    @DisplayName("Effect removed when Herald leaves the battlefield")
    void effectRemovedWhenHeraldLeaves() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Herald of Secret Streams"));

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Creature can be blocked again after counters are removed")
    void creatureCanBeBlockedAfterCountersRemoved() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters do not prevent blocking")
    void unrelatedCountersDoNotPreventBlocking() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfSecretStreams());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);
        herald.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasCantBeBlocked(gd, herald)).isFalse();
    }

    @Test
    @DisplayName("A creature that receives a counter after Herald enters cannot be blocked")
    void rejectsBlockAfterCounterIsAdded() {
        Permanent attacker = addCreatureReady(player1, new HeraldOfSecretStreams());
        addCreatureReady(player2, new HeraldOfSecretStreams());
        assertThat(gqs.hasCantBeBlocked(gd, attacker)).isFalse();
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
