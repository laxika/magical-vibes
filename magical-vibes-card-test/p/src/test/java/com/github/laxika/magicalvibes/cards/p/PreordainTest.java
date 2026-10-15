package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Preordain.class})
class PreordainTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Preordain puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Preordain.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Preordain()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving Preordain enters scry state with 2 cards")
    void resolvingEntersScryState() {
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("After scry completes, draws one card")
    void afterScryDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Complete scry by keeping both on top
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        // Hand should have 1 card (spell left hand, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should have lost 1 card from draw
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Scry reorders top of library before draw")
    void scryReordersBeforeDraw() {
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top1 = deck.get(1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Reverse the top 2, then draw 1 — should draw what was originally at position 1
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top1);
    }

    @Test
    @DisplayName("Scry putting cards on bottom then drawing")
    void scryBottomThenDraw() {
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Put both on bottom, then draw 1 — should draw what was originally at position 2
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top2);
    }

    @Test
    @DisplayName("Preordain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Complete scry
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Preordain");
    }
    @Test
    @DisplayName("Scry can keep one card and bottom the other before drawing")
    void splitsCardsBetweenTopAndBottom() {
        Card bottom = new Preordain();
        Card drawn = new Preordain();
        Card remaining = new Preordain();
        harness.setLibrary(player1, List.of(bottom, drawn, remaining));
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, bottom);
        harness.assertInGraveyard(player1, "Preordain");
    }

    @Test
    @DisplayName("Both cards can be bottomed in reverse order")
    void reversesBottomOrder() {
        Card first = new Preordain();
        Card second = new Preordain();
        Card drawn = new Preordain();
        Card remaining = new Preordain();
        harness.setLibrary(player1, List.of(first, second, drawn, remaining));
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, second, first);
    }

    @Test
    @DisplayName("With two cards, bottoming both still draws the first in the chosen bottom order")
    void drawsFromBottomedTwoCardLibrary() {
        Card first = new Preordain();
        Card second = new Preordain();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("A one-card library can bottom its only card and then draw it")
    void scriesOneCardLibrary() {
        Card only = new Preordain();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(only);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("An empty library skips the scry prompt and loses on the required draw")
    void emptyLibraryStillAttemptsDraw() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Preordain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
