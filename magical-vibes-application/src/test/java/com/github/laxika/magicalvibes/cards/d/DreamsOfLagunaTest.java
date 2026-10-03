package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TownGreeter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamsOfLaguna.class, TownGreeter.class})
class DreamsOfLagunaTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the surveilled card into the graveyard before drawing")
    void surveilsThenDraws() {
        Card surveilledCard = new TownGreeter();
        Card drawnCard = new TownGreeter();
        harness.setLibrary(player1, List.of(surveilledCard, drawnCard));
        harness.castFromHand(player1, new DreamsOfLaguna(), "{1}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Draws the top card when surveil is declined")
    void declinedSurveilDrawsTopCard() {
        Card topCard = new TownGreeter();
        Card remainingCard = new TownGreeter();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.castFromHand(player1, new DreamsOfLaguna(), "{1}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExiles() {
        Card flashbackCard = new DreamsOfLaguna();
        Card surveilledCard = new TownGreeter();
        Card drawnCard = new TownGreeter();
        harness.setGraveyard(player1, List.of(flashbackCard));
        harness.setLibrary(player1, List.of(surveilledCard, drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flashbackCard);
    }

    @Test
    @DisplayName("Flashback still draws and exiles the spell when surveil is declined")
    void flashbackWithDeclinedSurveil() {
        Card spell = new DreamsOfLaguna();
        Card topCard = new TownGreeter();
        Card remainingCard = new TownGreeter();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The normal mana cost is insufficient to cast with flashback")
    void flashbackRequiresItsFullCost() {
        Card spell = new DreamsOfLaguna();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Keeping the only library card allows the draw to succeed")
    void keepsAndDrawsOnlyLibraryCard() {
        Card spell = new DreamsOfLaguna();
        Card topCard = new TownGreeter();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, spell, "{1}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not skip the mandatory draw")
    void emptyLibraryStillAttemptsDraw() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new DreamsOfLaguna(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Putting the only library card into the graveyard leaves no card to draw")
    void surveillingOnlyCardStillAttemptsDraw() {
        Card topCard = new TownGreeter();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new DreamsOfLaguna(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
