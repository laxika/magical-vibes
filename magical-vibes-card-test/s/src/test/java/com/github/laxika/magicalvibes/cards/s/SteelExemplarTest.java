package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelExemplar.class})
class SteelExemplarTest extends BaseCardTest {

    private Permanent castAndResolve() {
        harness.setHand(player1, List.of(new SteelExemplar()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Steel Exemplar");
    }

    @Test
    @DisplayName("Enters with two +1/+1 counters when only one color is spent")
    void oneColorAddsCounters() {
        harness.addMana(player1, ManaColor.GREEN, 5);

        Permanent exemplar = castAndResolve();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, exemplar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, exemplar)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not get counters when two colors are spent")
    void twoColorsSkipCounters() {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent exemplar = castAndResolve();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, exemplar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, exemplar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Colorless mana does not count as a color")
    void colorlessManaAddsCounters() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Permanent exemplar = castAndResolve();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({
            "WHITE, BLUE", "WHITE, BLACK", "WHITE, RED", "WHITE, GREEN",
            "BLUE, BLACK", "BLUE, RED", "BLUE, GREEN",
            "BLACK, RED", "BLACK, GREEN", "RED, GREEN"
    })
    void everyTwoColorPairSkipsCounters(ManaColor first, ManaColor second) {
        harness.addMana(player1, first, 2);
        harness.addMana(player1, second, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent exemplar = castAndResolve();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void oneColorMixedWithColorlessStillAddsCounters(ManaColor color) {
        harness.addMana(player1, color, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent exemplar = castAndResolve();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastAddsCountersImmediately() {
        Permanent exemplar = harness.enterBattlefieldAndReturn(player1, new SteelExemplar());

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
