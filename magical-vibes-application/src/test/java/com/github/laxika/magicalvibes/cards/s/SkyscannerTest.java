package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skyscanner.class, Murder.class})
class SkyscannerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void enteringBattlefieldDrawsCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skyscanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBeforeCasting = gd.playerHands.get(player1.getId()).size();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Skyscanner");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCasting);
    }

    @Test
    @DisplayName("The draw waits for the entry trigger to resolve")
    void drawWaitsForTriggerResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skyscanner()));
        Skyscanner topCard = new Skyscanner();
        harness.setLibrary(player1, List.of(topCard, new Skyscanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyscanner");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing Skyscanner does not stop its controller drawing")
    void drawResolvesAfterSourceIsDestroyed() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Skyscanner()));
        harness.setHand(player1, List.of(new Murder()));
        Skyscanner topCard = new Skyscanner();
        harness.setLibrary(player2, List.of(topCard, new Skyscanner()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Skyscanner"));

        harness.assertNotOnBattlefield(player2, "Skyscanner");
        harness.assertInGraveyard(player2, "Skyscanner");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
