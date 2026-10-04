package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HungeringHydra.class, Shock.class, GreenwoodSentinel.class})
class HungeringHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Hungering Hydra enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new HungeringHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hungering Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Hungering Hydra gets counters equal to nonlethal damage dealt to it")
    void getsCountersEqualToDamageDealt() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new HungeringHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Hungering Hydra does not get counters after lethal damage")
    void doesNotGetCountersAfterLethalDamage() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new HungeringHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hungering Hydra");
    }

    @Test
    @DisplayName("Hungering Hydra cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HungeringHydra());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Hungering Hydra cast with X zero dies without counters")
    void diesWhenCastWithZeroX() {
        harness.setHand(player1, List.of(new HungeringHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hungering Hydra");
        assertThat(countPermanents(player1, "Hungering Hydra")).isZero();
    }

    @Test
    @DisplayName("A single blocker is legal and its combat damage grows Hungering Hydra")
    void singleBlockerDealsDamageAndGrowsHydra() {
        Permanent hydra = addCreatureReady(player1, new HungeringHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hungering Hydra")).isSameAs(hydra);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Separate damage events each add their own damage amount as counters")
    void separateDamageEventsEachGrowHydra() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new HungeringHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        resolveAllTriggers();

        assertThat(findPermanent(player2, "Hungering Hydra")).isSameAs(hydra);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
    }

    @Test
    @DisplayName("Pending damage triggers do not save Hungering Hydra from a second lethal hit")
    void diesToSecondHitBeforeCountersResolve() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new HungeringHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hungering Hydra");
        assertThat(countPermanents(player2, "Hungering Hydra")).isZero();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
