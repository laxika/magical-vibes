package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndlessCockroachesTest extends BaseCardTest {

    @Test
    @DisplayName("When Endless Cockroaches dies, it returns to its owner's hand instead of staying in the graveyard")
    void diesReturnsToOwnersHand() {
        harness.addToBattlefield(player1, new EndlessCockroaches());
        Permanent roaches = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card roachesCard = roaches.getCard();

        // Player 1 wraths the board — the black Cockroaches dies (Doom Blade can't hit black),
        // and its ON_DEATH trigger goes on the stack.
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Cockroaches dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — return to hand

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(roachesCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(roachesCard.getId()));
    }
}
