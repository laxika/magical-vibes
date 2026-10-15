package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({StormboundGeist.class, AirElemental.class, GrizzlyBears.class, LightningBolt.class,
        GrafdiggersCage.class})
class StormboundGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Stormbound Geist can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent geist = addCreatureReady(player2, new StormboundGeist());

        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(geist.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stormbound Geist cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new StormboundGeist());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Undying returns Stormbound Geist with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());
        harness.passBothPriorities();

        Permanent returnedGeist = findPermanent(player1, "Stormbound Geist");
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Stormbound Geist");
    }

    @Test
    @DisplayName("Undying does not return Stormbound Geist when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        geist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());

        harness.assertNotOnBattlefield(player1, "Stormbound Geist");
        harness.assertInGraveyard(player1, "Stormbound Geist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Stormbound Geist")
    void cannotBeBlockedByGroundCreature() {
        Permanent geist = addCreatureReady(player1, new StormboundGeist());
        geist.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(geist.isBlockedThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Undying returns a Geist that died with only -1/-1 counters")
    void undyingReturnsAfterMinusCounters() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        geist.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Stormbound Geist");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent returnedGeist = findPermanent(player1, "Stormbound Geist");
        assertThat(returnedGeist.getId()).isNotEqualTo(geist.getId());
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedGeist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Stormbound Geist");
    }

    @Test
    @DisplayName("Undying does not trigger when lethal -1/-1 counters coincide with counter cancellation")
    void simultaneousDeathUsesCountersBeforeCancellation() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        geist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        geist.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Stormbound Geist");
        harness.assertInGraveyard(player1, "Stormbound Geist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents undying from returning Stormbound Geist")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        geist.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Stormbound Geist");
        harness.assertInGraveyard(player1, "Stormbound Geist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen Stormbound Geist returns under its owner's control")
    void undyingReturnsToOwner() {
        Permanent geist = harness.addToBattlefieldAndReturn(player2, new StormboundGeist());
        gd.stolenCreatures.put(geist.getId(), player1.getId());
        geist.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Stormbound Geist");
        harness.assertNotInGraveyard(player2, "Stormbound Geist");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Stormbound Geist");
        Permanent returnedGeist = findPermanent(player1, "Stormbound Geist");
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Stormbound Geist");
    }
}
