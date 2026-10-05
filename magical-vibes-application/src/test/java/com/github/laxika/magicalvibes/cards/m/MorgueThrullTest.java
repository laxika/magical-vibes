package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MorgueThrull.class)
class MorgueThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Morgue Thrull mills three cards from its controller's library")
    void sacrificingMillsThreeCards() {
        harness.addToBattlefield(player1, new MorgueThrull());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        harness.setLibrary(player1, deck.subList(deck.size() - 5, deck.size()));
        deck = gd.playerDecks.get(player1.getId());
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deck).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Morgue Thrull");
    }

    @Test
    @DisplayName("Mills only the cards remaining when its controller has fewer than three cards")
    void millsOnlyCardsRemainingInShortLibrary() {
        harness.addToBattlefield(player1, new MorgueThrull());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        harness.setLibrary(player1, deck.subList(deck.size() - 2, deck.size()));
        deck = gd.playerDecks.get(player1.getId());
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deck).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore + 3);
    }

    @Test
    @DisplayName("Morgue Thrull's sacrifice cost is paid before milling resolves")
    void sacrificeIsPaidOnActivation() {
        harness.addToBattlefield(player1, new MorgueThrull());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Morgue Thrull");
        harness.assertInGraveyard(player1, "Morgue Thrull");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Morgue Thrull can be sacrificed while summoning sick")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new MorgueThrull());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Morgue Thrull");
    }

    @Test
    @DisplayName("Morgue Thrull can be sacrificed with an empty library")
    void canActivateWithEmptyLibrary() {
        harness.addToBattlefield(player1, new MorgueThrull());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Morgue Thrull");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Morgue Thrull controlled by the second player mills only that player's top three cards")
    void tappedThrullMillsItsControllersTopThreeCards() {
        harness.addToBattlefield(player2, new MorgueThrull());
        findPermanent(player2, "Morgue Thrull").setTapped(true);
        List<Card> library = List.of(new MorgueThrull(), new MorgueThrull(),
                new MorgueThrull(), new MorgueThrull());
        harness.setLibrary(player2, library);
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(3));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(4).containsAll(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Morgue Thrull");
    }
}
