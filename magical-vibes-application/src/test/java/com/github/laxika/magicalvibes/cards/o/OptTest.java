package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Opt.class})
class OptTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Opt puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Opt(), "{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Opt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving Opt enters scry state with 1 card")
    void resolvingEntersScryState() {
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("After scry completes, draws one card")
    void afterScryDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();

        // Complete scry by keeping the card on top
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        // Hand should have 1 card (spell left hand, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should have lost 1 card from draw
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Scry putting card on bottom then drawing")
    void scryBottomThenDraw() {
        harness.castFromHand(player1, new Opt(), "{U}");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top1 = deck.get(1);
        harness.passBothPriorities();

        // Put the top card on bottom, then draw 1 — should draw what was originally at position 1
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top1);
    }

    @Test
    @DisplayName("Opt still draws and causes an empty-library loss when there is nothing to scry")
    void emptyLibraryStillDrawsAfterScry() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opt goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();

        // Complete scry
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Opt");
    }
}
