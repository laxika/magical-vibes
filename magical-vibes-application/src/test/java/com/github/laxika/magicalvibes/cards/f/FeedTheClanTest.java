package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class FeedTheClanTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 5 life without ferocious")
    void gainsFiveLifeWithoutFerocious() {
        harness.setHand(player1, List.of(new FeedTheClan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Gains 10 life with ferocious")
    void gainsTenLifeWithFerocious() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new FeedTheClan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
    }
}
