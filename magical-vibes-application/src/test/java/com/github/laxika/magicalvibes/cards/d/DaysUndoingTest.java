package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaysUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles hand and graveyard away and draws seven")
    void eachPlayerDrawsSeven() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DaysUndoing()));
        harness.setHand(player2, List.of(handCard));
        gd.playerGraveyards.get(player2.getId()).add(graveyardCard);

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        cast();

        // Player 2 may hold an extra card: ending the turn hands them the next turn's draw.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSizeGreaterThanOrEqualTo(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c == handCard);
    }

    @Test
    @DisplayName("Resolving on your own turn ends the turn")
    void endsTheTurnOnYourTurn() {
        harness.setHand(player1, List.of(new DaysUndoing()));
        harness.setHand(player2, List.of());
        fillDeck(player1, 20);
        fillDeck(player2, 20);

        cast();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("The turn ends.")).isTrue();
    }

    private void cast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void fillDeck(Player player, int count) {
        List<Card> deck = gd.playerDecks.computeIfAbsent(player.getId(), id -> new ArrayList<>());
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
    }
}
