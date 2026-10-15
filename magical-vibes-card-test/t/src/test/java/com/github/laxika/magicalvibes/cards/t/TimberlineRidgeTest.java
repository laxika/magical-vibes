package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimberlineRidge.class, BloodMoon.class})
class TimberlineRidgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for red adds {R} and puts a depletion counter on the land")
    void tapsForRedAndAddsDepletionCounter() {
        Permanent ridge = addTimberlineRidge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for green adds {G} and puts a depletion counter on the land")
    void tapsForGreenAndAddsDepletionCounter() {
        Permanent ridge = addTimberlineRidge();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Timberline Ridge with a depletion counter stays tapped through the untap step, then the upkeep trigger removes the counter")
    void doesNotUntapWithDepletionCounterThenUpkeepRemovesIt() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ridge.isTapped()).isTrue();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Each upkeep removes only one depletion counter")
    void upkeepRemovesOnlyOneDepletionCounter() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.DEPLETION, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ridge.isTapped()).isTrue();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Timberline Ridge with no depletion counter untaps normally")
    void untapsWithoutDepletionCounter() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana and depletion counters are produced immediately without using the stack")
    void manaAbilityResolvesWithoutTheStack() {
        Permanent ridge = addTimberlineRidge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The depletion counter is removed on resolution, not when upkeep begins")
    void upkeepCounterRemovalUsesTheStack() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player1);

        assertThat(ridge.isTapped()).isTrue();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isZero();
        assertThat(ridge.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove depletion counters")
    void opponentUpkeepDoesNotRemoveCounters() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other counter types neither prevent untapping nor get removed during upkeep")
    void unrelatedCountersDoNotPreventUntapping() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.CHARGE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ridge.isTapped()).isFalse();
        assertThat(ridge.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Blood Moon removes the depletion untap restriction and upkeep ability")
    void bloodMoonAllowsUntappingWithDepletionCounter() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.DEPLETION, 1);
        harness.addToBattlefield(player2, new BloodMoon());

        advanceToUpkeep(player1);

        assertThat(ridge.isTapped()).isFalse();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTimberlineRidge() {
        Permanent ridge = harness.addToBattlefieldAndReturn(player1, new TimberlineRidge());
        ridge.setSummoningSick(false);
        return ridge;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
