package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwayOfTheStars.class, GnarledMass.class})
class SwayOfTheStarsTest extends BaseCardTest {

    @Test
    @DisplayName("Every permanent is shuffled away and each player draws seven")
    void resetsBoardAndRefillsHands() {
        harness.setHand(player2, List.of(new GnarledMass(), new GnarledMass()));
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addToBattlefield(player2, new GnarledMass());
        harness.addToBattlefield(player2, new GnarledMass());

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        castSway();

        assertThat(gd.playerBattlefields.getOrDefault(player1.getId(), java.util.List.of())).isEmpty();
        assertThat(gd.playerBattlefields.getOrDefault(player2.getId(), java.util.List.of())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Each player's life total becomes 7, whether they were above or below it")
    void setsBothLifeTotalsToSeven() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        castSway();

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 7);
    }

    @Test
    @DisplayName("Permanents go into their owner's library, not the graveyard")
    void permanentsGoToLibraryNotGraveyard() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GnarledMass());

        fillDeck(player1, 20);
        fillDeck(player2, 20);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castSway();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // one permanent joined the library, then seven cards were drawn out of it
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore + 1 - 7);
    }

    @Test
    @DisplayName("Hand and graveyard are shuffled in as well")
    void handAndGraveyardAreShuffledIn() {
        harness.setHand(player2, List.of(new GnarledMass(), new GnarledMass()));
        harness.setGraveyard(player2, List.of(new GnarledMass()));

        fillDeck(player1, 20);
        fillDeck(player2, 20);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castSway();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore + 2 + 1 - 7);
    }

    @Test
    @DisplayName("Sway of the Stars itself is not shuffled in — it ends up in its controller's graveyard")
    void spellEndsInGraveyard() {
        fillDeck(player1, 20);
        fillDeck(player2, 20);

        castSway();

        harness.assertInGraveyard(player1, "Sway of the Stars");
    }

    @Test
    @DisplayName("A permanent is shuffled into its owner's library even when another player controls it")
    void movesStolenPermanentToItsOwnersLibrary() {
        harness.setHand(player2, List.of());
        var stolen = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());

        fillDeck(player1, 20);
        fillDeck(player2, 20);
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        castSway();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore + 1 - 7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 7);
    }

    private void castSway() {
        harness.castFromHand(player1, new SwayOfTheStars(), "{8}{U}{U}");
        harness.passBothPriorities();
    }

    private void fillDeck(Player player, int count) {
        List<Card> deck = new ArrayList<>(gd.playerDecks.getOrDefault(player.getId(), List.of()));
        for (int i = 0; i < count; i++) {
            deck.add(new GnarledMass());
        }
        harness.setLibrary(player, deck);
    }
}
