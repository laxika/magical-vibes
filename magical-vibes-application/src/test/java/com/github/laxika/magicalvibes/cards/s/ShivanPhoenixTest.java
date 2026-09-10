package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShivanPhoenix.class, WrathOfGod.class})
class ShivanPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("When Shivan Phoenix dies, it returns to its owner's hand")
    void diesReturnsToOwnersHand() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new ShivanPhoenix());
        Card phoenixCard = phoenix.getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenixCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenixCard.getId()));
    }

    @Test
    @DisplayName("When controlled by an opponent, it returns to its owner's hand")
    void diesReturnsToOwnersHandWhenControlledByOpponent() {
        ShivanPhoenix phoenixCard = new ShivanPhoenix();
        phoenixCard.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, phoenixCard);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenixCard.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(phoenixCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenixCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(phoenixCard.getId()));
    }
}
