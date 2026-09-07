package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonragersSlash.class})
class MoonragersSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Costs only red mana and deals 3 damage at night")
    void costsLessAndDealsDamageAtNight() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not receive the cost reduction when it is not night")
    void doesNotCostLessWhenNotNight() {
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
