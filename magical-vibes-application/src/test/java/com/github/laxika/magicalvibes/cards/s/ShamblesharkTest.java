package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shambleshark.class, GrizzlyBears.class, Scorchwalker.class, Slaughterhorn.class,
        FrilledOculus.class, BurstOfStrength.class})
class ShamblesharkTest extends BaseCardTest {

    @Test
    @DisplayName("Flash lets Shambleshark be cast during the declare attackers step")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Shambleshark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shambleshark");
    }

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Shambleshark when a tougher creature enters")
    void evolvesForTougherCreature() {
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new Shambleshark());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when neither stat is greater")
    void doesNotEvolveForEqualCreature() {
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new Shambleshark());

        harness.setHand(player1, List.of(new Shambleshark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolvesForGreaterPowerWithEqualToughness() {
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new Shambleshark());

        harness.setHand(player1, List.of(new Scorchwalker()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveForOpponentsCreature() {
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new Shambleshark());

        harness.enterBattlefieldAndReturn(player2, new Scorchwalker());
        resolveAllTriggers();

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksConditionWhenSharkGrowsInResponse() {
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new Shambleshark());
        harness.enterBattlefieldAndReturn(player1, new FrilledOculus());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength(), new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, shark.getId());
        harness.castAndResolveInstant(player1, 0, shark.getId());
        resolveAllTriggers();

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsOnlyOneCounterWhenBothStatsAreGreater() {
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new Shambleshark());
        harness.enterBattlefieldAndReturn(player1, new Slaughterhorn());
        resolveAllTriggers();

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
