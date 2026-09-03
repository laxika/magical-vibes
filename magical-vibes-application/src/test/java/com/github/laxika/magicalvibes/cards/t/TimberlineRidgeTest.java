package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TimberlineRidge.class)
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

    private Permanent addTimberlineRidge() {
        Permanent ridge = harness.addToBattlefieldAndReturn(player1, new TimberlineRidge());
        ridge.setSummoningSick(false);
        return ridge;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
