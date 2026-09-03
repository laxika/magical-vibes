package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
