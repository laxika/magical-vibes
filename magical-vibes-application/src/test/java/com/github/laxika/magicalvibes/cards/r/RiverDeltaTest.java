package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RiverDelta.class)
class RiverDeltaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for blue adds {U} and puts a depletion counter on the land")
    void tapsForBlueAndAddsDepletionCounter() {
        Permanent riverDelta = addRiverDelta();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(riverDelta.isTapped()).isTrue();
        assertThat(riverDelta.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for black adds {B} and puts a depletion counter on the land")
    void tapsForBlackAndAddsDepletionCounter() {
        Permanent riverDelta = addRiverDelta();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.BLACK)).isEqualTo(1);
        assertThat(riverDelta.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("A River Delta with a depletion counter stays tapped through the untap step, then the upkeep trigger removes the counter")
    void doesNotUntapWithDepletionCounterThenUpkeepRemovesIt() {
        Permanent riverDelta = addRiverDelta();
        riverDelta.tap();
        riverDelta.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(riverDelta.isTapped()).isTrue();
        assertThat(riverDelta.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Each upkeep removes only one depletion counter")
    void upkeepRemovesOnlyOneDepletionCounter() {
        Permanent riverDelta = addRiverDelta();
        riverDelta.tap();
        riverDelta.setCounterCount(CounterType.DEPLETION, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(riverDelta.isTapped()).isTrue();
        assertThat(riverDelta.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("A River Delta with no depletion counter untaps normally")
    void untapsWithoutDepletionCounter() {
        Permanent riverDelta = addRiverDelta();
        riverDelta.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(riverDelta.isTapped()).isFalse();
    }

    private Permanent addRiverDelta() {
        Permanent riverDelta = harness.addToBattlefieldAndReturn(player1, new RiverDelta());
        riverDelta.setSummoningSick(false);
        return riverDelta;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
