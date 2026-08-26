package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineRaider.class, WilyGoblin.class})
class MineRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when you control another outlaw")
    void createsTreasureWithAnotherOutlaw() {
        harness.addToBattlefield(player1, new WilyGoblin());
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count itself as another outlaw")
    void doesNotCreateTreasureWithoutAnotherOutlaw() {
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("An opponent's outlaw does not satisfy the condition")
    void opponentOutlawDoesNotCount() {
        harness.addToBattlefield(player2, new WilyGoblin());
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private void castMineRaider() {
        harness.setHand(player1, List.of(new MineRaider()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
