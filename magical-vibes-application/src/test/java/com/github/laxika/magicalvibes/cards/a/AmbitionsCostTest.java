package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbitionsCost.class, AngelicPage.class})
class AmbitionsCostTest extends BaseCardTest {

    private void setLibraryForDraw() {
        harness.setLibrary(player1, List.of(new AngelicPage(), new AngelicPage(), new AngelicPage()));
    }

    private void cast() {
        harness.castFromHand(player1, new AmbitionsCost(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        setLibraryForDraw();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Resolving loses three life")
    void losesThreeLife() {
        harness.setLife(player1, 20);

        cast();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Only affects its controller")
    void onlyAffectsItsController() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        cast();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }
}
