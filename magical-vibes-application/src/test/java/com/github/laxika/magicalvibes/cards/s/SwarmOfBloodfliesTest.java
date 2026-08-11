package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SwarmOfBloodfliesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        castSwarm();

        Permanent swarm = findSwarm();
        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter whenever another creature dies")
    void getsCounterWhenAnotherCreatureDies() {
        castSwarm();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent swarm = findSwarm();
        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    private void castSwarm() {
        harness.setHand(player1, List.of(new SwarmOfBloodflies()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findSwarm() {
        return findPermanent(player1, "Swarm of Bloodflies");
    }
}
