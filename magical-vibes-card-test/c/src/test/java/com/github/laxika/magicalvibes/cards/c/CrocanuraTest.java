package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BasilicaGuards;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.e.ExperimentOne;
import com.github.laxika.magicalvibes.cards.s.SimicCharm;
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

@CardUsed({Crocanura.class, DiscipleOfTheOldWays.class, ExperimentOne.class,
        BasilicaGuards.class, BurstOfStrength.class, SimicCharm.class, CloudfinRaptor.class})
class CrocanuraTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Crocanura when the entering creature has greater power")
    void evolvesWhenEnteringCreatureHasGreaterPower() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());

        harness.setHand(player1, List.of(new DiscipleOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger for a creature with lesser power and toughness")
    void doesNotEvolveForSmallerCreature() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());

        harness.setHand(player1, List.of(new ExperimentOne()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolvesForGreaterToughnessWithEqualPower() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());
        harness.enterBattlefieldAndReturn(player1, new BasilicaGuards());
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveForEqualPowerAndToughness() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());
        harness.enterBattlefieldAndReturn(player1, new Crocanura());
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotEvolveForOpponentsCreature() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());
        harness.enterBattlefieldAndReturn(player2, new BasilicaGuards());
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksConditionWhenCrocanuraGrowsInResponse() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());
        harness.enterBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, crocanura.getId());
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void usesChangedStatsAsEnteringCreatureLastExistedBeforeReturningToHand() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());
        Permanent disciple = harness.enterBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength(), new BurstOfStrength(), new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, crocanura.getId());
        harness.castAndResolveInstant(player1, 0, disciple.getId());
        harness.castInstant(player1, 0, 2, disciple.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Disciple of the Old Ways");
        harness.assertNotOnBattlefield(player1, "Disciple of the Old Ways");
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void separateTriggersRecheckAfterFirstCounterIsPlaced() {
        Permanent crocanura = harness.addToBattlefieldAndReturn(player1, new Crocanura());
        harness.enterBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        harness.enterBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(crocanura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());
        attacker.setSummoningSick(false);
        Permanent crocanura = harness.addToBattlefieldAndReturn(player2, new Crocanura());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(crocanura.isBlocking()).isTrue();
        assertThat(crocanura.getBlockingTargets()).containsExactly(0);
    }
}
