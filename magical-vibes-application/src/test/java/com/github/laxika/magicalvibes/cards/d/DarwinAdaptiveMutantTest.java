package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarwinAdaptiveMutant.class, GrizzlyBears.class})
class DarwinAdaptiveMutantTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Darwin when a larger creature enters")
    void evolvesWhenLargerCreatureEnters() {
        Permanent darwin = harness.addToBattlefieldAndReturn(player1, new DarwinAdaptiveMutant());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(darwin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing two +1/+1 counters grants indestructible until end of turn")
    void removesCountersAndGrantsIndestructible() {
        Permanent darwin = addDarwinReady();
        darwin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(darwin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, darwin, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, darwin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without two +1/+1 counters")
    void cannotActivateWithoutTwoCounters() {
        addDarwinReady().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Evolve does not trigger when neither entering stat is greater")
    void doesNotEvolveWhenNeitherStatIsGreater() {
        Permanent darwin = harness.addToBattlefieldAndReturn(player1, new DarwinAdaptiveMutant());
        darwin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(darwin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve rechecks the comparison when its trigger resolves")
    void rechecksStatsWhenEvolveResolves() {
        Permanent darwin = harness.addToBattlefieldAndReturn(player1, new DarwinAdaptiveMutant());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        darwin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(darwin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped and summoning-sick Darwin pays counters immediately and gains indestructible on resolution")
    void activatesWhileTappedAndSummoningSick() {
        Permanent darwin = harness.addToBattlefieldAndReturn(player1, new DarwinAdaptiveMutant());
        darwin.setTapped(true);
        darwin.setSummoningSick(true);
        darwin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(darwin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, darwin, Keyword.INDESTRUCTIBLE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, darwin, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(darwin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addDarwinReady() {
        return addCreatureReady(player1, new DarwinAdaptiveMutant());
    }
}
