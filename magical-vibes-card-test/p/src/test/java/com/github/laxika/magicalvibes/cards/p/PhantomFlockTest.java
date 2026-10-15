package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.s.SuddenStrength;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberShot.class, FlaringPain.class, LavaDart.class, Malignus.class, PhantomFlock.class, SuddenStrength.class, SuntailHawk.class})
class PhantomFlockTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.castFromHand(player1, new PhantomFlock(), "{3}{W}{W}");
        harness.passBothPriorities();

        Permanent flock = findPermanent(player1, "Phantom Flock");
        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents damage and removes one +1/+1 counter")
    void preventsDamageAndRemovesOneCounter() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, flock.getId());

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(flock.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removes only one counter for each separate damage event")
    void removesOneCounterPerDamageEvent() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new LavaDart(), new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, flock.getId());
        harness.castAndResolveInstant(player1, 0, flock.getId());

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flock.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still prevents damage after its last counter is removed")
    void preventsDamageWithoutCounters() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, flock.getId());

        harness.setHand(player1, List.of(new LavaDart(), new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, flock.getId());
        harness.castAndResolveInstant(player1, 0, flock.getId());

        Permanent survivingFlock = findPermanent(player2, "Phantom Flock");
        assertThat(survivingFlock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(survivingFlock.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage is prevented and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounter() {
        Permanent blocker = addCreatureReady(player2, new PhantomFlock());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        addCreatureReady(player1, new SuntailHawk());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still prevents damage when no +1/+1 counters remain")
    void preventsDamageWithoutCountersJudReview() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setToughnessModifier(3);

        dealEmberShotForJudReview(flock);

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(flock.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flock);
    }

    @Test
    @DisplayName("Unpreventable damage still removes one +1/+1 counter")
    void unpreventableDamageStillRemovesOneCounter() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        flock.setToughnessModifier(3);

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        dealEmberShotForJudReview(flock);

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(flock.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flock);
    }

    @Test
    @DisplayName("Simultaneous damage from multiple sources removes only one counter")
    void simultaneousDamageFromMultipleSourcesRemovesOnlyOneCounter() {
        Permanent flock = addCreatureReady(player1, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent firstBlocker = addCreatureReady(player2, new SuntailHawk());
        Permanent secondBlocker = addCreatureReady(player2, new SuntailHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 2));

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(flock.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Unpreventable combat damage from Malignus still removes a counter")
    void sourceSpecificUnpreventableCombatDamageStillRemovesCounter() {
        harness.setLife(player2, 2);
        addCreatureReady(player1, new Malignus());
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(flock.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Phantom Flock");
    }

    @Test
    @DisplayName("Removing the last counter prevents damage but puts the unboosted creature in the graveyard")
    void removingLastCounterCausesZeroToughnessDeath() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, flock.getId());

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(flock.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Phantom Flock");
        harness.assertInGraveyard(player2, "Phantom Flock");
    }

    private void dealEmberShotForJudReview(Permanent target) {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
