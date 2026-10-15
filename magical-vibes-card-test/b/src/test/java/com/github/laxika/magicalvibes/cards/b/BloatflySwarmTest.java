package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloatflySwarm.class, Shock.class, DressDown.class})
class BloatflySwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five +1/+1 counters")
    void entersWithFiveCounters() {
        Permanent swarm = harness.enterBattlefieldAndReturn(player1, new BloatflySwarm());

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prevents damage, removes counters, and gives each player matching rad counters")
    void preventsDamageRemovesCountersAndGivesRadCounters() {
        Permanent swarm = harness.enterBattlefieldAndReturn(player2, new BloatflySwarm());
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, swarm.getId());

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(swarm.getMarkedDamage()).isZero();
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("The rad-counter follow-up resolves when the last counter is removed")
    void lastCounterRemovalGivesRadCountersBeforeTheSwarmDies() {
        Permanent swarm = harness.enterBattlefieldAndReturn(player2, new BloatflySwarm());
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, swarm.getId());

        harness.assertInGraveyard(player2, "Bloatfly Swarm");
        assertThat(gd.playerRadCounters.get(player1.getId())).isOne();
        assertThat(gd.playerRadCounters.get(player2.getId())).isOne();
    }

    @Test
    @DisplayName("Damage is dealt normally while the swarm has lost all abilities")
    void doesNotPreventDamageAfterLosingAbilities() {
        Permanent swarm = harness.enterBattlefieldAndReturn(player2, new BloatflySwarm());
        harness.addToBattlefield(player1, new DressDown());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, swarm.getId());

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(swarm.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertOnBattlefield(player2, "Bloatfly Swarm");
    }

    @Test
    @DisplayName("Repeated damage removes counters and accumulates rad counters for both players")
    void repeatedDamageAccumulatesRadCounters() {
        Permanent swarm = harness.enterBattlefieldAndReturn(player2, new BloatflySwarm());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, swarm.getId());
        harness.castAndResolveInstant(player1, 0, swarm.getId());

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(swarm.getMarkedDamage()).isZero();
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Bloatfly Swarm");
    }

    @Test
    @DisplayName("Enters without counters and dies when Dress Down is already on the battlefield")
    void entersWithoutCountersWhileAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new DressDown());
        harness.setHand(player1, List.of(new BloatflySwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bloatfly Swarm");
        harness.assertInGraveyard(player1, "Bloatfly Swarm");
        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
