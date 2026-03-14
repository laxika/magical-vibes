package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PonderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ponder has reorder 3, may shuffle, and draw 1 spell effects")
    void hasCorrectEffects() {
        Ponder card = new Ponder();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ReorderTopCardsOfLibraryEffect.class);
        ReorderTopCardsOfLibraryEffect reorderEffect = (ReorderTopCardsOfLibraryEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(reorderEffect.count()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(mayEffect.wrapped()).isInstanceOf(ShuffleLibraryEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(2);
        assertThat(drawEffect.amount()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Ponder puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ponder");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Ponder()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — library reorder =====

    @Test
    @DisplayName("Resolving Ponder enters library reorder state with 3 cards")
    void resolvingEntersLibraryReorderState() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryReorderContext()).isNotNull();
        assertThat(gd.interaction.libraryReorderContext().cards()).hasSize(3);
    }

    // ===== Resolving — reorder then decline shuffle, then draw =====

    @Test
    @DisplayName("After reorder, player is asked to shuffle")
    void afterReorderPlayerIsAskedToShuffle() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Complete reorder (keep same order)
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Declining shuffle and drawing keeps top card")
    void declineShuffleDrawsTopCard() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Keep same order
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));

        // Decline shuffle
        harness.handleMayAbilityChosen(player1, false);

        // Should have drawn the top card
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top0);
    }

    @Test
    @DisplayName("Reordering changes which card is drawn")
    void reorderingChangesDrawnCard() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Put card at index 2 on top
        gs.handleLibraryCardsReordered(gd, player1, List.of(2, 0, 1));

        // Decline shuffle
        harness.handleMayAbilityChosen(player1, false);

        // Should have drawn what was originally at index 2
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top2);
    }

    // ===== Resolving — reorder then accept shuffle, then draw =====

    @Test
    @DisplayName("Accepting shuffle randomizes the library before draw")
    void acceptShuffleRandomizesBeforeDraw() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Complete reorder
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));

        // Accept shuffle
        harness.handleMayAbilityChosen(player1, true);

        // Should still have drawn 1 card
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        // Deck should have lost 1 card from draw
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Goes to graveyard =====

    @Test
    @DisplayName("Ponder goes to graveyard after fully resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Complete reorder
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));

        // Decline shuffle
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ponder"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Accepting shuffle logs shuffle message")
    void acceptingShuffleLogsMessage() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Declining shuffle logs decline message")
    void decliningShuffleLogsMessage() {
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declines"));
    }
}
