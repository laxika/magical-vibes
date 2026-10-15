package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CavernHarpy;
import com.github.laxika.magicalvibes.cards.w.WarpedDevotion;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PygmyKavu.class, PhyrexianScuta.class, AlphaKavu.class, WarpedDevotion.class, CavernHarpy.class})
class PygmyKavuTest extends BaseCardTest {

    private void stockLibraryWithAlphaKavus(int count) {
        harness.setLibrary(player1, IntStream.range(0, count)
                .mapToObj(i -> new AlphaKavu()).toList());
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

    @Test
    @DisplayName("ETB counts a multicolored black creature once")
    void etbCountsMulticoloredBlackCreatureOnce() {
        addCreatureReady(player2, new CavernHarpy());
        stockLibraryWithAlphaKavus(5);

        harness.castFromHand(player1, new PygmyKavu(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB counts black creatures when the trigger resolves")
    void etbCountsCreaturesAtResolution() {
        stockLibraryWithAlphaKavus(5);

        harness.castFromHand(player1, new PygmyKavu(), "{3}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        addCreatureReady(player2, new PhyrexianScuta());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering without being cast still triggers the draw")
    void enteringWithoutCastingTriggersDraw() {
        addCreatureReady(player2, new PhyrexianScuta());
        stockLibraryWithAlphaKavus(5);

        harness.enterBattlefieldAndReturn(player1, new PygmyKavu());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
