package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ZoneOfFlame.class)
class ZoneOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent when it enters")
    void triggersOnBattlefieldEntries() {
        harness.setHand(player1, List.of(new ZoneOfFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
