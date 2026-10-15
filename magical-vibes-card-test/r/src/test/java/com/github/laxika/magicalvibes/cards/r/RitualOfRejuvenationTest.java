package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitualOfRejuvenation.class})
class RitualOfRejuvenationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ritual of Rejuvenation puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new RitualOfRejuvenation(), "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RitualOfRejuvenation.class);
    }

    @Test
    @DisplayName("Ritual of Rejuvenation gains 4 life for its controller")
    void gains4Life() {
        harness.setLife(player1, 16);
        harness.castFromHand(player1, new RitualOfRejuvenation(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ritual of Rejuvenation draws a card for its controller")
    void drawsACard() {
        harness.castFromHand(player1, new RitualOfRejuvenation(), "{2}{W}");
        harness.passBothPriorities();

        // Hand had 1 card (the spell), cast it (0 cards), then drew 1 card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's life total is unaffected")
    void opponentLifeUnaffected() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new RitualOfRejuvenation(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ritual of Rejuvenation goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.castFromHand(player1, new RitualOfRejuvenation(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ritual of Rejuvenation");
    }

    @Test
    @DisplayName("Life gain can increase the controller's life total above 20")
    void gainsLifeAboveStartingTotal() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RitualOfRejuvenation(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("The other player gains life and draws exactly the top card")
    void otherControllerGainsLifeAndDraws() {
        RitualOfRejuvenation topCard = new RitualOfRejuvenation();
        RitualOfRejuvenation remainingCard = new RitualOfRejuvenation();
        harness.setLibrary(player2, List.of(topCard, remainingCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 15);
        harness.setLife(player2, 12);

        harness.castFromHand(player2, new RitualOfRejuvenation(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 15);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Ritual of Rejuvenation");
    }
}
