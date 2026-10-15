package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupplyRunners.class, Plains.class})
class SupplyRunnersTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability puts a +1/+1 counter on each other creature you control")
    void putsCountersOnOtherCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SupplyRunners());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SupplyRunners());

        harness.castFromHand(player1, new SupplyRunners(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent supplyRunners = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(supplyRunners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters are put on every other creature, including another Supply Runners, but not lands")
    void countersEachOtherCreatureButNotNoncreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SupplyRunners());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SupplyRunners());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.castFromHand(player1, new SupplyRunners(), "{4}{W}");
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger resolves with no other creatures and does not put a counter on itself")
    void resolvesWithNoOtherCreatures() {
        harness.castFromHand(player1, new SupplyRunners(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Supply Runners")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures to receive counters are determined when the trigger resolves")
    void includesCreatureThatArrivesBeforeResolution() {
        harness.castFromHand(player1, new SupplyRunners(), "{4}{W}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Supply Runners");
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new SupplyRunners());
        harness.passBothPriorities();

        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
