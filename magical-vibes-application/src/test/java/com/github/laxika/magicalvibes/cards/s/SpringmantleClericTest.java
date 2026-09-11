package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SpringmantleCleric.class)
class SpringmantleClericTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each color of mana spent")
    void entersWithCountersForEachColorSpent() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent cleric = castAndResolve();

        assertThat(cleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(8);
    }

    @Test
    @DisplayName("Repeated mana of one color counts only once")
    void repeatedManaOfOneColorCountsOnce() {
        harness.addMana(player1, ManaColor.GREEN, 5);

        Permanent cleric = castAndResolve();

        assertThat(cleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(4);
    }

    @Test
    @DisplayName("Colorless mana for the generic cost does not add counters")
    void colorlessManaDoesNotAddCounter() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent cleric = castAndResolve();

        assertThat(cleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(4);
    }

    @Test
    @DisplayName("Colorless mana alongside colored mana counts only the colored colors")
    void colorlessManaAlongsideColoredManaCountsOnlyColors() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent cleric = castAndResolve();

        assertThat(cleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent castAndResolve() {
        harness.setHand(player1, List.of(new SpringmantleCleric()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Springmantle Cleric"))
                .findFirst().orElseThrow();
    }
}
