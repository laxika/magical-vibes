package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanarVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles non-token cards put into either player's graveyard")
    void exilesCardsPutIntoEitherGraveyard() {
        harness.addToBattlefield(player1, new PlanarVoid());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new HillGiant();
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, ownCard);
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, opponentCard);

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ownPermanent);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opponentPermanent);
        });

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    @DisplayName("Does not exile the card if it leaves the graveyard before the trigger resolves")
    void doesNotExileCardThatLeavesGraveyardBeforeResolution() {
        harness.addToBattlefield(player1, new PlanarVoid());
        Card card = new GrizzlyBears();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        gd.playerGraveyards.get(player1.getId()).remove(card);
        gd.playerHands.get(player1.getId()).add(card);

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }
}
