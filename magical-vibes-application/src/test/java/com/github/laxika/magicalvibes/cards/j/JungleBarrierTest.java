package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JungleBarrier.class})
class JungleBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        JungleBarrier drawnCard = new JungleBarrier();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new JungleBarrier(), "{2}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Defender prevents Jungle Barrier from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new JungleBarrier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Entry draw waits for its trigger and survives removal of Jungle Barrier")
    void entryDrawResolvesAfterSourceLeaves() {
        JungleBarrier drawnCard = new JungleBarrier();
        JungleBarrier remainingCard = new JungleBarrier();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));

        harness.castFromHand(player1, new JungleBarrier(), "{2}{G}{U}");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jungle Barrier");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, findPermanent(player1, "Jungle Barrier")));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Jungle Barrier");
        harness.assertInGraveyard(player1, "Jungle Barrier");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Entering without being cast draws for the entering creature's controller")
    void noncastEntryDrawsForController() {
        JungleBarrier drawnCard = new JungleBarrier();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new JungleBarrier());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Jungle Barrier");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
