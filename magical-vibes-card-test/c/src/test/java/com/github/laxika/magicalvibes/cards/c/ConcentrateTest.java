package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Concentrate.class, CabalPit.class, CarefulStudy.class, CephalidScout.class})
class ConcentrateTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards")
    void drawsThreeCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new Concentrate(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws the top three cards from its controller's library")
    void drawsTopThreeCards() {
        CabalPit cabalPit = new CabalPit();
        CarefulStudy carefulStudy = new CarefulStudy();
        CephalidScout cephalidScout = new CephalidScout();
        harness.setLibrary(player1, List.of(cabalPit, carefulStudy, cephalidScout));

        harness.castFromHand(player1, new Concentrate(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cabalPit, carefulStudy, cephalidScout);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the spell's controller draws cards")
    void onlyControllerDrawsCards() {
        int controllerDeckSize = gd.playerDecks.get(player2.getId()).size();
        int otherDeckSize = gd.playerDecks.get(player1.getId()).size();
        int otherHandSize = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new Concentrate(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckSize - 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(otherHandSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(otherDeckSize);
        harness.assertInGraveyard(player2, "Concentrate");
    }

    @Test
    @DisplayName("Draws the remaining cards and loses when the third draw finds an empty library")
    void losesWhenLibraryHasFewerThanThreeCards() {
        CabalPit cabalPit = new CabalPit();
        CarefulStudy carefulStudy = new CarefulStudy();
        harness.setLibrary(player1, List.of(cabalPit, carefulStudy));

        harness.castFromHand(player1, new Concentrate(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cabalPit, carefulStudy);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
