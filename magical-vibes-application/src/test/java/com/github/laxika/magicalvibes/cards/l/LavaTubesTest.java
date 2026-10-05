package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LavaTubes.class)
class LavaTubesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for black adds {B} and puts a depletion counter on the land")
    void tapsForBlackAndAddsDepletionCounter() {
        Permanent lavaTubes = addLavaTubes();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.BLACK)).isEqualTo(1);
        assertThat(lavaTubes.isTapped()).isTrue();
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for red adds {R} and puts a depletion counter on the land")
    void tapsForRedAndAddsDepletionCounter() {
        Permanent lavaTubes = addLavaTubes();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lava Tubes with a depletion counter stays tapped through the untap step, then the upkeep trigger removes the counter")
    void doesNotUntapWithDepletionCounterThenUpkeepRemovesIt() {
        Permanent lavaTubes = addLavaTubes();
        lavaTubes.tap();
        lavaTubes.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lavaTubes.isTapped()).isTrue();
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Each upkeep removes only one depletion counter")
    void upkeepRemovesOnlyOneDepletionCounter() {
        Permanent lavaTubes = addLavaTubes();
        lavaTubes.tap();
        lavaTubes.setCounterCount(CounterType.DEPLETION, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lavaTubes.isTapped()).isTrue();
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lava Tubes with no depletion counter untaps normally")
    void untapsWithoutDepletionCounter() {
        Permanent lavaTubes = addLavaTubes();
        lavaTubes.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lavaTubes.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The mana ability resolves immediately, but depletion removal uses the stack")
    void depletionRemovalWaitsForUpkeepTriggerResolution() {
        Permanent lavaTubes = addLavaTubes();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(lavaTubes.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isZero();
        assertThat(lavaTubes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove depletion counters")
    void opponentsUpkeepDoesNotRemoveCounter() {
        Permanent lavaTubes = addLavaTubes();
        lavaTubes.tap();
        lavaTubes.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(lavaTubes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Lava Tubes can produce mana even with a depletion counter")
    void existingCounterDoesNotPreventManaActivation() {
        Permanent lavaTubes = addLavaTubes();
        lavaTubes.setCounterCount(CounterType.DEPLETION, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(lavaTubes.isTapped()).isTrue();
        assertThat(lavaTubes.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addLavaTubes() {
        Permanent lavaTubes = harness.addToBattlefieldAndReturn(player1, new LavaTubes());
        lavaTubes.setSummoningSick(false);
        return lavaTubes;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
