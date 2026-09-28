package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HandOfCruelty;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PresenceOfTheWise.class, HandOfHonor.class, HandOfCruelty.class})
class PresenceOfTheWiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each card remaining in hand")
    void gainsTwoLifePerCardInHand() {
        harness.setHand(player1, List.of(new PresenceOfTheWise(), new HandOfHonor(), new HandOfCruelty()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Gains no life when no cards remain in hand")
    void gainsNoLifeWithEmptyHand() {
        harness.setHand(player1, List.of(new PresenceOfTheWise()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count cards in the opponent's hand")
    void countsOnlyCardsInControllerHand() {
        harness.setHand(player1, List.of(new PresenceOfTheWise(), new HandOfHonor()));
        harness.setHand(player2, List.of(new HandOfHonor(), new HandOfCruelty()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
