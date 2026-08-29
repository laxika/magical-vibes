package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmblazonedGolem.class})
class EmblazonedGolemTest extends BaseCardTest {

    @Test
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new EmblazonedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Emblazoned Golem");
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithKickerXPlusOneCounters() {
        harness.setHand(player1, List.of(new EmblazonedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        castWithKickerX(3);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Emblazoned Golem");
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void kickerXCannotUseColorlessMana() {
        harness.setHand(player1, List.of(new EmblazonedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> castWithKickerX(2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void kickerXCannotSpendTheSameColorTwice() {
        harness.setHand(player1, List.of(new EmblazonedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> castWithKickerX(2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    private void castWithKickerX(int xValue) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, xValue, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true);
    }
}
