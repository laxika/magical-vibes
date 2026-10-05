package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OozeFlux.class, GrizzlyBears.class})
class OozeFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Removes the chosen number of counters and creates an X/X Ooze")
    void createsOozeSizedByCountersRemoved() {
        addFlux();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addMana();

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);
    }

    @Test
    @DisplayName("Leaves the counters not paid for on the creature")
    void removesOnlyTheChosenCounters() {
        addFlux();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addMana();

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Ooze"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires at least one counter to be removed")
    void rejectsZeroCounters() {
        addFlux();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Ooze")).isZero();
    }

    @Test
    @DisplayName("Cannot remove more counters than the creatures you control have")
    void rejectsMoreCountersThanAvailable() {
        addFlux();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Ooze")).isZero();
    }

    @Test
    @DisplayName("Counters on an opponent's creature can't pay the cost")
    void ignoresOpponentCounters() {
        addFlux();
        Permanent theirBears = addCreatureReady(player2, new GrizzlyBears());
        theirBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(theirBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(countPermanents(player1, "Ooze")).isZero();
    }

    @Test
    @DisplayName("Pays counters before resolution and combines counters from multiple creatures")
    void combinesCountersFromMultipleCreaturesAsAnActivationCost() {
        addFlux();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addMana();

        harness.activateAbility(player1, 0, 0, 2, null);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Ooze")).isZero();

        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lets the controller choose which creatures supply the counters")
    void choosesTheCounterDistribution() {
        addFlux();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addMana();

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters on a noncreature permanent cannot pay the cost")
    void ignoresNoncreatureCounters() {
        addFlux();
        Permanent flux = findPermanent(player1, "Ooze Flux");
        flux.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(flux.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(countPermanents(player1, "Ooze")).isZero();
    }

    private void addFlux() {
        harness.addToBattlefield(player1, new OozeFlux());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
