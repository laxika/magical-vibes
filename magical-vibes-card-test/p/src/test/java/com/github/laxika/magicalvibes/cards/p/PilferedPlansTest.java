package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PilferedPlans.class})
class PilferedPlansTest extends BaseCardTest {

    private void castAt(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PilferedPlans()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Target player mills two cards and the controller draws two")
    void millsTwoAndDrawsTwo() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Can target yourself, milling then drawing from your own library")
    void canTargetSelf() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        castAt(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
        harness.assertInGraveyard(player1, "Pilfered Plans");
    }

    @Test
    @DisplayName("Draws two even when the target's library is empty")
    void drawsEvenWithEmptyTargetLibrary() {
        harness.setLibrary(player2, List.of());

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milled cards come from the top of the target's library")
    void millsFromTopOfLibrary() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card top = deck.get(0);
        Card third = deck.get(2);

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(top);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(third);
    }

    @Test
    @DisplayName("Mills the only remaining card and still draws two")
    void millsOneWithShortTargetLibrary() {
        Card remaining = new PilferedPlans();
        harness.setLibrary(player2, List.of(remaining));

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Self-targeting mills the first two cards before drawing the next two")
    void millsBeforeDrawingWhenTargetingSelf() {
        Card first = new PilferedPlans();
        Card second = new PilferedPlans();
        Card third = new PilferedPlans();
        Card fourth = new PilferedPlans();
        Card fifth = new PilferedPlans();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));

        castAt(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        harness.assertInGraveyard(player1, "Pilfered Plans");
    }
}
