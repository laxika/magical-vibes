package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Veldt.class)
class VeldtTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for green adds {G} and puts a depletion counter on the land")
    void tapsForGreenAndAddsDepletionCounter() {
        Permanent veldt = addVeldt();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(veldt.isTapped()).isTrue();
        assertThat(veldt.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for white adds {W} and puts a depletion counter on the land")
    void tapsForWhiteAndAddsDepletionCounter() {
        Permanent veldt = addVeldt();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.WHITE)).isEqualTo(1);
        assertThat(veldt.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Veldt with a depletion counter stays tapped through the untap step, then the upkeep trigger removes the counter")
    void doesNotUntapWithDepletionCounterThenUpkeepRemovesIt() {
        Permanent veldt = addVeldt();
        veldt.tap();
        veldt.setCounterCount(CounterType.DEPLETION, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(veldt.isTapped()).isTrue();
        assertThat(veldt.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Veldt's upkeep trigger removes only one depletion counter")
    void upkeepRemovesOnlyOneDepletionCounter() {
        Permanent veldt = addVeldt();
        veldt.tap();
        veldt.setCounterCount(CounterType.DEPLETION, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(veldt.isTapped()).isTrue();
        assertThat(veldt.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Veldt with no depletion counter untaps normally")
    void untapsWithoutDepletionCounter() {
        Permanent veldt = addVeldt();
        veldt.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(veldt.isTapped()).isFalse();
    }

    private Permanent addVeldt() {
        Permanent veldt = harness.addToBattlefieldAndReturn(player1, new Veldt());
        veldt.setSummoningSick(false);
        return veldt;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
