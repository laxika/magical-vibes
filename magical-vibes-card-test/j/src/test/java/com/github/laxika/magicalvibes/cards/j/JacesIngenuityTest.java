package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacesIngenuity.class})
class JacesIngenuityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Jace's Ingenuity puts it on the stack")
    void castingPutsOnStack() {
        JacesIngenuity spell = new JacesIngenuity();
        harness.castFromHand(player1, spell, "{3}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isSameAs(spell);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving draws three cards")
    void resolvingDrawsThreeCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();

        // Hand should have 3 cards (spell left hand, then drew 3)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Deck should have lost 3 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Jace's Ingenuity goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jace's Ingenuity");
    }

    @Test
    @DisplayName("Player two draws the last three cards without losing or making player one draw")
    void playerTwoDrawsLastThreeCardsWithoutLosing() {
        JacesIngenuity first = new JacesIngenuity();
        JacesIngenuity second = new JacesIngenuity();
        JacesIngenuity third = new JacesIngenuity();
        harness.setLibrary(player2, List.of(first, second, third));
        int opponentHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player2, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("With two cards left, draws both and loses on the third draw")
    void shortLibraryCausesLossAfterDrawingRemainingCards() {
        JacesIngenuity first = new JacesIngenuity();
        JacesIngenuity second = new JacesIngenuity();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
