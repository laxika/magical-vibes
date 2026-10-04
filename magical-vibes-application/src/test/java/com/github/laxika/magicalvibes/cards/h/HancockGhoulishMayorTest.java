package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MistformMutant;
import com.github.laxika.magicalvibes.cards.z.ZombieBrute;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HancockGhoulishMayor.class, ZombieBrute.class, MistformMutant.class, GrizzlyBears.class,
        LightningBolt.class})
class HancockGhoulishMayorTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies and Mutants you control get +X/+X for Hancock's counters")
    void boostsOtherZombiesAndMutantsBasedOnCounters() {
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        Permanent mutant = addCreatureReady(player1, new MistformMutant());
        Permanent nonmatching = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentZombie = addCreatureReady(player2, new ZombieBrute());
        int zombiePower = gqs.getEffectivePower(gd, zombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, zombie);
        int mutantPower = gqs.getEffectivePower(gd, mutant);
        int mutantToughness = gqs.getEffectiveToughness(gd, mutant);
        int nonmatchingPower = gqs.getEffectivePower(gd, nonmatching);
        int nonmatchingToughness = gqs.getEffectiveToughness(gd, nonmatching);
        int opponentPower = gqs.getEffectivePower(gd, opponentZombie);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponentZombie);

        Permanent hancock = addCreatureReady(player1, new HancockGhoulishMayor());
        hancock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 2);
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(mutantPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(mutantToughness + 2);
        assertThat(gqs.getEffectivePower(gd, nonmatching)).isEqualTo(nonmatchingPower);
        assertThat(gqs.getEffectiveToughness(gd, nonmatching)).isEqualTo(nonmatchingToughness);
        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(opponentToughness);
    }

    @Test
    @DisplayName("Hancock does not boost itself")
    void doesNotBoostItself() {
        Permanent hancock = addCreatureReady(player1, new HancockGhoulishMayor());
        int basePower = gqs.getEffectivePower(gd, hancock);
        int baseToughness = gqs.getEffectiveToughness(gd, hancock);

        hancock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.getEffectivePower(gd, hancock)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, hancock)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Undying returns Hancock with a +1/+1 counter")
    void undyingReturnsWithCounter() {
        Permanent hancock = harness.addToBattlefieldAndReturn(player1, new HancockGhoulishMayor());
        harness.setHand(player1, java.util.List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, hancock.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Hancock, Ghoulish Mayor");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Hancock, Ghoulish Mayor");
    }

    @Test
    @DisplayName("All counter types on Hancock contribute to the boost and it updates immediately")
    void countsAllCounterTypesAndUpdatesBoost() {
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        Permanent mutant = addCreatureReady(player1, new MistformMutant());
        int zombiePower = gqs.getEffectivePower(gd, zombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, zombie);
        int mutantPower = gqs.getEffectivePower(gd, mutant);
        int mutantToughness = gqs.getEffectiveToughness(gd, mutant);
        Permanent hancock = addCreatureReady(player1, new HancockGhoulishMayor());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness);
        hancock.setCounterCount(CounterType.CHARGE, 2);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 2);
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(mutantPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(mutantToughness + 2);

        hancock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 3);
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(mutantPower + 3);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(mutantToughness + 3);

        hancock.setCounterCount(CounterType.CHARGE, 0);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 1);
    }

    @Test
    @DisplayName("Undying does not return Hancock if it dies with a +1/+1 counter")
    void undyingDoesNotReturnWithPlusOneCounter() {
        Permanent hancock = harness.addToBattlefieldAndReturn(player1, new HancockGhoulishMayor());
        hancock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, java.util.List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, hancock.getId());

        harness.assertNotOnBattlefield(player1, "Hancock, Ghoulish Mayor");
        harness.assertInGraveyard(player1, "Hancock, Ghoulish Mayor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Other counter types do not prevent undying and disappear when Hancock returns")
    void undyingReturnsWithOtherCounterTypes() {
        Permanent hancock = harness.addToBattlefieldAndReturn(player1, new HancockGhoulishMayor());
        hancock.setCounterCount(CounterType.CHARGE, 2);
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        harness.setHand(player1, java.util.List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, hancock.getId());
        harness.assertNotOnBattlefield(player1, "Hancock, Ghoulish Mayor");
        int zombiePower = gqs.getEffectivePower(gd, zombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, zombie);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Hancock, Ghoulish Mayor");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 1);
        harness.assertNotInGraveyard(player1, "Hancock, Ghoulish Mayor");
    }
}
