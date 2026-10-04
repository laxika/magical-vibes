package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessHorizons.class, Plains.class, Forest.class})
class EndlessHorizonsTest extends BaseCardTest {

    private void castAndResolveEtb(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EndlessHorizons()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → library search
    }

    @Test
    @DisplayName("ETB search offers only Plains cards from the library")
    void etbSearchOffersOnlyPlains() {
        castAndResolveEtb(List.of(new Plains(), new Forest(), new Plains(), new Forest()));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("ETB exiles the chosen Plains tracked with the enchantment; non-Plains stay in the library")
    void etbExilesChosenPlainsTrackedWithSource() {
        castAndResolveEtb(List.of(new Plains(), new Forest(), new Plains(), new Forest()));

        UUID permId = harness.getPermanentId(player1, "Endless Horizons");

        harness.handleCardChosen(player1, 0); // exile first Plains, re-prompt
        harness.handleCardChosen(player1, 0); // exile second Plains, no matches remain → done

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(permId)).allMatch(c -> c.getName().equals("Plains"));
        // The two Forests remain in the library.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).allMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Controller may stop the any-number search early")
    void etbCanStopEarly() {
        castAndResolveEtb(List.of(new Plains(), new Plains(), new Forest()));

        UUID permId = harness.getPermanentId(player1, "Endless Horizons");

        harness.handleCardChosen(player1, 0);  // exile one Plains
        harness.handleCardChosen(player1, -1); // decline the rest

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        // The unexiled Plains is still in the library.
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("With no Plains in the library, no search prompt is created")
    void etbNoPlainsNoSearch() {
        castAndResolveEtb(List.of(new Forest(), new Forest()));

        UUID permId = harness.getPermanentId(player1, "Endless Horizons");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    private UUID setupWithExiledCards(List<? extends Card> cards) {
        harness.addToBattlefield(player1, new EndlessHorizons());
        UUID permId = harness.getPermanentId(player1, "Endless Horizons");
        for (Card card : cards) {
            gd.addToExile(player1.getId(), card, permId);
        }
        return permId;
    }

    private void advanceToSecondTurnUpkeep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
    }

    @Test
    @DisplayName("Accepting the upkeep trigger returns a single exiled Plains to hand")
    void upkeepReturnsSingleExiledPlainsToHand() {
        UUID permId = setupWithExiledCards(List.of(new Plains()));
        UUID exiledId = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(exiledId));
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the card exiled")
    void upkeepDeclineLeavesCardExiled() {
        UUID permId = setupWithExiledCards(List.of(new Plains()));

        UUID exiledId = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        // The card is not returned: it stays exiled with the enchantment.
        assertThat(gd.getCardsExiledByPermanent(permId))
                .hasSize(1)
                .anyMatch(c -> c.getId().equals(exiledId));
    }

    @Test
    @DisplayName("With several exiled cards, the controller chooses one to return")
    void upkeepChoosesOneOfSeveral() {
        UUID permId = setupWithExiledCards(List.of(new Plains(), new Plains(), new Plains()));
        UUID chosen = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(chosen));
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(permId)).noneMatch(c -> c.getId().equals(chosen));
    }

    @Test
    @DisplayName("Accepting the upkeep trigger returns any card exiled with the enchantment")
    void upkeepReturnsAnyOwnedCardExiledWithSource() {
        UUID permId = setupWithExiledCards(List.of(new Forest()));
        UUID exiledId = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(exiledId));
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability returns only cards the controller owns")
    void upkeepReturnsOnlyCardsOwnedByController() {
        UUID permId = setupWithExiledCards(List.of(new Plains()));
        UUID ownedCardId = gd.getCardsExiledByPermanent(permId).getFirst().getId();
        Forest opponentOwnedCard = new Forest();
        gd.addToExile(player2.getId(), opponentOwnedCard, permId);

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(ownedCardId));
        assertThat(gd.getCardsExiledByPermanent(permId))
                .hasSize(1)
                .anyMatch(c -> c.getId().equals(opponentOwnedCard.getId()));
    }

    @Test
    @DisplayName("The search may exile zero Plains even when Plains are available")
    void etbCanChooseZeroCards() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        castAndResolveEtb(List.of(plains, forest));
        UUID sourceId = harness.getPermanentId(player1, "Endless Horizons");

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(sourceId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, forest);
    }

    @Test
    @DisplayName("Endless Horizons does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotReturnCard() {
        UUID sourceId = setupWithExiledCards(List.of(new Plains()));
        UUID exiledId = gd.getCardsExiledByPermanent(sourceId).getFirst().getId();

        advanceToSecondTurnUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(sourceId)).extracting(Card::getId).containsExactly(exiledId);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(exiledId));
    }

    @Test
    @DisplayName("The upkeep ability returns a Plains exiled by the actual enters trigger")
    void upkeepReturnsCardExiledByEtb() {
        Plains plains = new Plains();
        castAndResolveEtb(List.of(plains, new Forest()));
        UUID sourceId = harness.getPermanentId(player1, "Endless Horizons");
        harness.handleCardChosen(player1, 0);

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.getCardsExiledByPermanent(sourceId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(plains);
    }
}
