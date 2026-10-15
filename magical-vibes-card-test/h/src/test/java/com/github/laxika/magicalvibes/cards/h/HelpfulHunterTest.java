package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Helpful Hunter")
@CardUsed({HelpfulHunter.class, SavannahLions.class})
class HelpfulHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setHand(player1, List.of(new HelpfulHunter()));
        harness.setLibrary(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Savannah Lions");
    }

    @Test
    @DisplayName("Draw waits for the enter trigger and draws exactly one card")
    void drawWaitsForTriggerResolution() {
        HelpfulHunter topCard = new HelpfulHunter();
        HelpfulHunter secondCard = new HelpfulHunter();
        harness.setHand(player1, List.of(new HelpfulHunter()));
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast draws for the creature's controller")
    void enteringWithoutCastingDrawsForController() {
        HelpfulHunter drawnCard = new HelpfulHunter();
        HelpfulHunter opponentCard = new HelpfulHunter();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(opponentCard));
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new HelpfulHunter());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }
}
