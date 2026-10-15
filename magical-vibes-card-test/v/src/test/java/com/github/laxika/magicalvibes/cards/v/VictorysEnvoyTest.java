package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictorysEnvoy.class, GrizzlyBears.class, Plains.class})
class VictorysEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Each Envoy counters the other Envoy but not noncreature permanents")
    void multipleEnvoysExcludeOnlyTheirOwnSource() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VictorysEnvoy());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VictorysEnvoy());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An upkeep trigger resolves after the Envoy leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new VictorysEnvoy());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(envoy);
        gd.playerGraveyards.get(player1.getId()).add(envoy.getCard());
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after the trigger receive counters at resolution")
    void checksCreaturesAtResolution() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new VictorysEnvoy());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(envoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on each other creature you control")
    void putsCountersOnOtherCreaturesYouControl() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new VictorysEnvoy());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(envoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new VictorysEnvoy());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
