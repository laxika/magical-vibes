package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessCockroaches.class, WrathOfGod.class})
class EndlessCockroachesTest extends BaseCardTest {

    @Test
    @DisplayName("When Endless Cockroaches dies, it returns to its owner's hand instead of staying in the graveyard")
    void diesReturnsToOwnersHand() {
        Permanent roaches = harness.addToBattlefieldAndReturn(player1, new EndlessCockroaches());
        Card roachesCard = roaches.getCard();

        // Player 1 wraths the board — the black Cockroaches dies,
        // and its ON_DEATH trigger goes on the stack.
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves — Cockroaches dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — return to hand

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(roachesCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(roachesCard.getId()));
    }

    @Test
    @DisplayName("When an opponent-owned Endless Cockroaches dies, it returns to its owner's hand")
    void diesReturnsToOwnersHandWhenControlledByAnotherPlayer() {
        Card roachesCard = new EndlessCockroaches();
        roachesCard.setOwnerId(player2.getId());
        Permanent roaches = harness.addToBattlefieldAndReturn(player1, roachesCard);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(roaches.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(roaches.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(roaches.getCard().getId()));
    }
}
