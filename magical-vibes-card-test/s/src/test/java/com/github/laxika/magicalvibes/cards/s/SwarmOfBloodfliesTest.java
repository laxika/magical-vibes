package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.e.EndHostilities;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MurderousCut;
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

@CardUsed({SwarmOfBloodflies.class, GrizzlyBears.class, Shock.class, AlpineGrizzly.class,
        MurderousCut.class, EndHostilities.class})
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
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        Permanent swarm = findSwarm();
        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    void alliedCreatureDeathAddsCounterOnlyWhenTriggerResolves() {
        castSwarm();
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Alpine Grizzly"));

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        assertThat(gd.stack).hasSize(1);
        assertThat(findSwarm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(findSwarm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownDeathDoesNotTriggerItsAbility() {
        castSwarm();
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, findSwarm().getId());

        harness.assertNotOnBattlefield(player1, "Swarm of Bloodflies");
        harness.assertInGraveyard(player1, "Swarm of Bloodflies");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pendingTriggerCannotPutCountersOnSwarmAfterItDies() {
        castSwarm();
        Permanent swarm = findSwarm();
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new MurderousCut(), new MurderousCut()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Alpine Grizzly"));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, swarm.getId());

        harness.assertInGraveyard(player1, "Swarm of Bloodflies");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Swarm of Bloodflies");
        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void simultaneousDeathsTriggerOnceForEachOtherCreatureEvenWhenSwarmDies() {
        castSwarm();
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new EndHostilities()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Swarm of Bloodflies");
        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Swarm of Bloodflies");
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
