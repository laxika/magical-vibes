package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwayOfTheStarsTest extends BaseCardTest {

    @Test
    @DisplayName("Every permanent is shuffled away and each player draws seven")
    void resetsBoardAndRefillsHands() {
        harness.setHand(player1, List.of(new SwayOfTheStars()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

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
        harness.setHand(player1, List.of(new SwayOfTheStars()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        castSway();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Permanents go into their owner's library, not the graveyard")
    void permanentsGoToLibraryNotGraveyard() {
        harness.setHand(player1, List.of(new SwayOfTheStars()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());

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
        harness.setHand(player1, List.of(new SwayOfTheStars()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

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
        harness.setHand(player1, List.of(new SwayOfTheStars()));

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        castSway();

        harness.assertInGraveyard(player1, "Sway of the Stars");
    }

    private void castSway() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void fillDeck(Player player, int count) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        if (deck == null) {
            deck = new ArrayList<>();
            gd.playerDecks.put(player.getId(), deck);
        }
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
    }
}
