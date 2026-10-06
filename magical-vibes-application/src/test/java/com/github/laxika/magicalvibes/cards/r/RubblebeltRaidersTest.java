package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubblebeltRaiders.class, DiscipleOfTheOldWays.class, RapidHybridization.class})
class RubblebeltRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone puts a single +1/+1 counter on it (it counts itself)")
    void attackingAloneAddsOneCounter() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(raiders.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts every attacking creature you control, and only the Raiders get counters")
    void countsAllAttackersYouControl() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Non-attacking creatures you control are not counted")
    void ignoresNonAttackingCreatures() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures the opponent controls are not counted")
    void ignoresOpponentCreatures() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        harness.addToBattlefield(player2, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each attacking Raiders receives counters only from its own trigger")
    void multipleRaidersEachReceiveTheirOwnCounters() {
        Permanent first = addCreatureReady(player1, new RubblebeltRaiders());
        Permanent second = addCreatureReady(player1, new RubblebeltRaiders());
        Permanent idle = addCreatureReady(player1, new RubblebeltRaiders());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(idle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An attacker destroyed in response is excluded from the count at resolution")
    void countsAttackersAtResolution() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new RapidHybridization()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gd.stack).hasSize(1);
            harness.castAndResolveInstant(player1, 0, disciple.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Disciple of the Old Ways");
        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Frog Lizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing Raiders in response leaves its replacement token without counters")
    void removedSourceDoesNotPutCountersOnAnotherCreature() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new RapidHybridization()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            assertThat(gd.stack).hasSize(1);
            harness.castAndResolveInstant(player1, 0, raiders.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Rubblebelt Raiders");
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Frog Lizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
