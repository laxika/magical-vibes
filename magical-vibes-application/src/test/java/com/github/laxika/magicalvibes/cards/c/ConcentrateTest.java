package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
