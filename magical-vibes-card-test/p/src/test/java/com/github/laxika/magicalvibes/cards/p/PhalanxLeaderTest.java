package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhalanxLeader.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class PhalanxLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Phalanx Leader puts a counter on each creature you control")
    void castingSpellThatTargetsLeaderPutsCountersOnControlledCreatures() {
        harness.addToBattlefield(player1, new PhalanxLeader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID leaderId = harness.getPermanentId(player1, "Phalanx Leader");
        harness.castAndResolveInstant(player1, 0, leaderId);
        harness.passBothPriorities();

        Permanent leader = findPermanent(player1, "Phalanx Leader");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Phalanx Leader")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PhalanxLeader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent leader = findPermanent(player1, "Phalanx Leader");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Phalanx Leader does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PhalanxLeader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID leaderId = harness.getPermanentId(player1, "Phalanx Leader");
        harness.castAndResolveInstant(player2, 0, leaderId);

        Permanent leader = findPermanent(player1, "Phalanx Leader");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heroic resolves before the targeting spell and affects only controlled creatures")
    void heroicResolvesBeforeSpellAndExcludesOpponentsCreatures() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new PhalanxLeader());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, leader.getId());
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Targeting another creature does not trigger heroic")
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new PhalanxLeader());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only the targeted leader triggers and creatures entering before resolution get counters")
    void onlyTargetedLeaderTriggersAndUsesCreaturesPresentAtResolution() {
        Permanent targetedLeader = harness.addToBattlefieldAndReturn(player1, new PhalanxLeader());
        Permanent otherLeader = harness.addToBattlefieldAndReturn(player1, new PhalanxLeader());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetedLeader.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(targetedLeader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherLeader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Heroic still puts counters on surviving creatures after the leader dies")
    void heroicResolvesAfterLeaderLeavesBattlefield() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new PhalanxLeader());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, leader.getId());
        harness.castAndResolveInstant(player2, 0, leader.getId());
        harness.assertNotOnBattlefield(player1, "Phalanx Leader");
        harness.assertInGraveyard(player1, "Phalanx Leader");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Giant Growth");
    }
}
