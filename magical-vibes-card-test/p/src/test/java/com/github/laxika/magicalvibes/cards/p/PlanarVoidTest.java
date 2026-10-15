package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.w.Whetstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarVoid.class, ArgothianSwine.class, Duress.class, Whetstone.class})
class PlanarVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles non-token cards put into either player's graveyard")
    void exilesCardsPutIntoEitherGraveyard() {
        harness.addToBattlefield(player1, new PlanarVoid());
        Card ownCard = new ArgothianSwine();
        Card opponentCard = new ArgothianSwine();
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
        Card card = new ArgothianSwine();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        gd.playerGraveyards.get(player1.getId()).remove(card);
        gd.playerHands.get(player1.getId()).add(card);

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Exiles cards discarded from a hand")
    void exilesCardsDiscardedFromHand() {
        harness.addToBattlefield(player1, new PlanarVoid());
        Card discardedCard = new Duress();
        harness.setHand(player2, List.of(discardedCard));
        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discardedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(discardedCard);
    }

    @Test
    @DisplayName("Does not exile Planar Void when it is put into a graveyard")
    void doesNotExileItself() {
        Permanent planarVoid = harness.addToBattlefieldAndReturn(player1, new PlanarVoid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, planarVoid));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planarVoid.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(planarVoid.getCard());
    }

    @Test
    @DisplayName("Exiles a sorcery card after it finishes resolving")
    void exilesResolvedSorcery() {
        harness.addToBattlefield(player1, new PlanarVoid());
        Card spell = new Duress();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Exiles cards milled from both libraries")
    void exilesCardsMilledFromBothLibraries() {
        harness.addToBattlefield(player1, new Whetstone());
        harness.addToBattlefield(player1, new PlanarVoid());
        Card ownCard = new ArgothianSwine();
        Card opponentCard = new Duress();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    @DisplayName("A queued trigger still exiles its card after Planar Void leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent planarVoid = harness.addToBattlefieldAndReturn(player1, new PlanarVoid());
        Card card = new ArgothianSwine();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, planarVoid);
        });

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planarVoid.getCard());
    }

    @Test
    @DisplayName("An old trigger cannot exile a card that leaves the graveyard and later returns")
    void doesNotExileNewObjectAfterCardReturnsToGraveyard() {
        Permanent planarVoid = harness.addToBattlefieldAndReturn(player1, new PlanarVoid());
        Card card = new ArgothianSwine();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
        });
        Permanent returnedCreature = harness.enterBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, planarVoid);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returnedCreature);
        });

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }
}
