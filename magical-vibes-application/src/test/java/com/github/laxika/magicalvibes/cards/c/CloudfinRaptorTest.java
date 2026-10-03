package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DutifulThrull;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.cards.s.SimicCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudfinRaptor.class, DutifulThrull.class, BurstOfStrength.class,
        FrilledOculus.class, SimicCharm.class})
class CloudfinRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Cloudfin Raptor when power is greater")
    void evolvesWhenEnteringCreatureHasGreaterPower() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());

        harness.setHand(player1, List.of(new DutifulThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when the entering creature has equal power and toughness")
    void doesNotEvolveForEqualStats() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());

        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolvesForGreaterToughnessWithEqualPower() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());
        raptor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new FrilledOculus()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());

        harness.enterBattlefieldAndReturn(player2, new DutifulThrull());
        resolveAllTriggers();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksConditionWhenRaptorGrowsInResponse() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());
        harness.enterBattlefieldAndReturn(player1, new DutifulThrull());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, raptor.getId());
        resolveAllTriggers();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void usesChangedStatsAsCreatureLastExistedBeforeReturningToHand() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());
        Permanent thrull = harness.enterBattlefieldAndReturn(player1, new DutifulThrull());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength(), new BurstOfStrength(), new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, raptor.getId());
        harness.castAndResolveInstant(player1, 0, thrull.getId());
        harness.castInstant(player1, 0, 2, thrull.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Dutiful Thrull");
        harness.assertNotOnBattlefield(player1, "Dutiful Thrull");
        resolveAllTriggers();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
