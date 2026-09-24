package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.s.Stratadon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlametongueYearling.class, Stratadon.class, ManaCylix.class})
class FlametongueYearlingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to its power without multikicker")
    void etbDealsBasePowerDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stratadon());
        harness.setHand(player1, List.of(new FlametongueYearling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent yearling = findPermanent(player1, "Flametongue Yearling");
        assertThat(yearling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multikicker adds counters and increases ETB damage")
    void multikickerAddsCountersAndDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stratadon());
        harness.setHand(player1, List.of(new FlametongueYearling()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{2}", "{2}"), false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent yearling = findPermanent(player1, "Flametongue Yearling");
        assertThat(yearling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        harness.setHand(player1, List.of(new FlametongueYearling()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
