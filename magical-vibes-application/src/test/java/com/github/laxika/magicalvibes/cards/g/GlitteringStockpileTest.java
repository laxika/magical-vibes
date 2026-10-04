package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlitteringStockpile.class})
class GlitteringStockpileTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Glittering Stockpile adds red mana and a stash counter")
    void tappingAddsRedManaAndStashCounter() {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new GlitteringStockpile());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(stockpile.getCounterCount(CounterType.STASH)).isEqualTo(1);
        assertThat(stockpile.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Glittering Stockpile adds mana equal to its stash counters")
    void sacrificingAddsManaEqualToStashCounters() {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new GlitteringStockpile());
        stockpile.setCounterCount(CounterType.STASH, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stockpile);
        harness.assertInGraveyard(player1, "Glittering Stockpile");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void sacrificeProducesTheWholeAmountInOneChosenColor(ManaColor color) {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new GlitteringStockpile());
        stockpile.setCounterCount(CounterType.STASH, 4);
        stockpile.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Glittering Stockpile");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeWithNoStashCountersAddsNoMana() {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new GlitteringStockpile());
        stockpile.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Glittering Stockpile");
        harness.assertInGraveyard(player1, "Glittering Stockpile");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedTapsAccumulateStashCountersForSacrifice() {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new GlitteringStockpile());
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            stockpile.untap();
        }

        assertThat(stockpile.getCounterCount(CounterType.STASH)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Glittering Stockpile");
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {0, 1})
    void tappedStockpileCannotPayEitherTapCost(int abilityIndex) {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new GlitteringStockpile());
        stockpile.setCounterCount(CounterType.STASH, 3);
        stockpile.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Glittering Stockpile");
        harness.assertNotInGraveyard(player1, "Glittering Stockpile");
        assertThat(stockpile.getCounterCount(CounterType.STASH)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
