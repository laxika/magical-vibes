package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShoreLurker.class})
class ShoreLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers surveil 1 and accepts putting the top card into the graveyard")
    void entersWithSurveilAccepted() {
        Card topCard = new ShoreLurker();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castFromHand(player1, new ShoreLurker(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Declining the enters-the-battlefield surveil leaves the top card on the library")
    void entersWithSurveilDeclined() {
        Card topCard = new ShoreLurker();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new ShoreLurker(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Entering with an empty library still surveils without requiring a choice")
    void surveilsWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castFromHand(player1, new ShoreLurker(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Shore Lurker");
    }

    @Test
    @DisplayName("Entering without being cast surveils exactly one card from its controller's library")
    void enteringWithoutCastingSurveilsControllersLibrary() {
        Card topCard = new ShoreLurker();
        Card nextCard = new ShoreLurker();
        Card opponentTopCard = new ShoreLurker();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new ShoreLurker());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId()).doesNotContain(player2.getId());
    }
}
