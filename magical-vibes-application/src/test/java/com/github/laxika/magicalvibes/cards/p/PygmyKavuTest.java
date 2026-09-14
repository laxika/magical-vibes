package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.w.WarpedDevotion;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PygmyKavu.class, PhyrexianScuta.class, AlphaKavu.class, WarpedDevotion.class})
class PygmyKavuTest extends BaseCardTest {

    private void stockLibraryWithAlphaKavus(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new AlphaKavu());
        }
    }

    @Test
    @DisplayName("ETB draws a card for each black creature opponents control")
    void etbDrawsForEachBlackCreatureOpponentsControl() {
        addCreatureReady(player2, new PhyrexianScuta());
        addCreatureReady(player2, new PhyrexianScuta());
        addCreatureReady(player2, new AlphaKavu());
        stockLibraryWithAlphaKavus(5);

        harness.castFromHand(player1, new PygmyKavu(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB draws no cards when opponents control no black creatures")
    void etbDrawsNoCardsWithoutBlackOpposingCreatures() {
        addCreatureReady(player2, new AlphaKavu());
        stockLibraryWithAlphaKavus(5);

        harness.castFromHand(player1, new PygmyKavu(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB ignores black creatures controlled by you")
    void etbIgnoresBlackCreaturesControlledByYou() {
        addCreatureReady(player1, new PhyrexianScuta());
        addCreatureReady(player2, new PhyrexianScuta());
        stockLibraryWithAlphaKavus(5);

        harness.castFromHand(player1, new PygmyKavu(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB ignores black permanents that are not creatures")
    void etbIgnoresBlackNoncreaturePermanents() {
        addCreatureReady(player2, new PhyrexianScuta());
        harness.addToBattlefield(player2, new WarpedDevotion());
        stockLibraryWithAlphaKavus(5);

        harness.castFromHand(player1, new PygmyKavu(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
