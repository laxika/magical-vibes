package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CutADeal.class)
class CutADealTest extends BaseCardTest {

    @Test
    void eachOpponentDrawsThenControllerDrawsForEachOpponentWhoDrew() {
        CutADeal controllerDraw = new CutADeal();
        CutADeal opponentDraw = new CutADeal();
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(opponentDraw));

        harness.castFromHand(player1, new CutADeal(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cut a Deal");
    }
}
