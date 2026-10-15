package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.Excruciator;
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

@CardUsed({MagmaPummeler.class, Shock.class, Excruciator.class})
class MagmaPummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new MagmaPummeler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent pummeler = findPermanent(player1, "Magma Pummeler");
        assertThat(pummeler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents damage, removes matching counters, and deals that much damage to a target")
    void preventsDamageAndDealsRemovedAmount() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player2, new MagmaPummeler());
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pummeler.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Magma Pummeler").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Magma Pummeler").getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The reflexive damage still resolves after all counters are removed")
    void reflexiveDamageResolvesAfterPummelerDies() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player2, new MagmaPummeler());
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pummeler.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Magma Pummeler");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting with X=0 puts the creature into the graveyard without a damage trigger")
    void zeroXDiesWithoutTriggering() {
        harness.setHand(player1, List.of(new MagmaPummeler()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Magma Pummeler");
        harness.assertInGraveyard(player1, "Magma Pummeler");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Its own reflexive damage can target it and cause another damage trigger")
    void canTargetItselfAndTriggerAgain() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player2, new MagmaPummeler());
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, pummeler.getId());
        harness.handlePermanentChosen(player2, pummeler.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(pummeler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pummeler.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Unpreventable combat damage still removes counters and triggers damage")
    void unpreventableDamageStillRemovesCountersAndTriggers() {
        addCreatureReady(player1, new Excruciator());
        Permanent pummeler = addCreatureReady(player2, new MagmaPummeler());
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 15);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        assertThat(pummeler.getMarkedDamage()).isEqualTo(7);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Magma Pummeler");
        assertThat(pummeler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        harness.assertLife(player1, 13);
    }
}
