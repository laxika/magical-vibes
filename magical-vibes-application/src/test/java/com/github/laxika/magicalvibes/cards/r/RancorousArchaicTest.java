package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RancorousArchaicTest extends BaseCardTest {

    private Permanent castAndResolve() {
        harness.setHand(player1, List.of(new RancorousArchaic()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rancorous Archaic"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Cast with five colors of mana — enters with five +1/+1 counters as a 7/7")
    void fiveColorsGivesFiveCounters() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent archaic = castAndResolve();

        assertThat(archaic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, archaic)).isEqualTo(7);
    }

    @Test
    @DisplayName("Cast with a single color of mana — enters with one +1/+1 counter as a 3/3")
    void oneColorGivesOneCounter() {
        harness.addMana(player1, ManaColor.GREEN, 5);

        Permanent archaic = castAndResolve();

        assertThat(archaic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archaic)).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated mana of the same colors counts each color once")
    void repeatedColorsCountOnce() {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent archaic = castAndResolve();

        assertThat(archaic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(4);
    }

    @Test
    @DisplayName("Colorless mana does not count toward Converge — enters as a plain 2/2")
    void colorlessManaDoesNotCount() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Permanent archaic = castAndResolve();

        assertThat(archaic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archaic)).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless mana alongside colored mana counts only the colors")
    void colorlessAlongsideColoredCountsOnlyColors() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent archaic = castAndResolve();

        assertThat(archaic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
