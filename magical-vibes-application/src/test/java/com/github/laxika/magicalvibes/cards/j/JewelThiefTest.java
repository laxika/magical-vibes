package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@CardUsed(JewelThief.class)
class JewelThiefTest extends BaseCardTest {

    @Test
    @DisplayName("When Jewel Thief enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new JewelThief()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }
}
