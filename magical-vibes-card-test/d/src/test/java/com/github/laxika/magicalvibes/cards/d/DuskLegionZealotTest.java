package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskLegionZealot.class, Forest.class})
class DuskLegionZealotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card and loses 1 life")
    void etbDrawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new DuskLegionZealot(), "{1}{B}");

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Drawing and life loss wait for the entry trigger to resolve")
    void entryTriggerUsesTheStack() {
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new DuskLegionZealot(), "{1}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dusk Legion Zealot");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Entering without casting draws and loses life for its controller")
    void noncastEntryAffectsOnlyController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player2, new DuskLegionZealot());
        resolveAllTriggers();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(opponentLifeBefore);
    }
}
