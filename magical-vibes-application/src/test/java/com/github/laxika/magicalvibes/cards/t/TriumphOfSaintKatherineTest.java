package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphOfSaintKatherine.class, GrizzlyBears.class, Plains.class})
class TriumphOfSaintKatherineTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it is exiled and the top six cards of its controller's library are shuffled back on top")
    void deathTriggerExilesItAndShufflesTopSixCards() {
        List<Card> topCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        Card cardBelowPile = new Plains();
        harness.setLibrary(player1, List.of(
                topCards.get(0), topCards.get(1), topCards.get(2),
                topCards.get(3), topCards.get(4), topCards.get(5), cardBelowPile));
        Permanent triumph = harness.addToBattlefieldAndReturn(player1, new TriumphOfSaintKatherine());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, triumph));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(triumph.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(triumph.getCard());
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 6))
                .containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.playerDecks.get(player1.getId()).get(6)).isSameAs(cardBelowPile);
    }
}
