package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiftTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Sift has correct card properties")
    void hasCorrectProperties() {
        Sift card = new Sift();

        assertThat(card.getName()).isEqualTo("Sift");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(drawEffect.amount()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DiscardCardEffect.class);
        DiscardCardEffect discardEffect = (DiscardCardEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(discardEffect.amount()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Sift puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sift");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving draws three cards then prompts for discard")
    void resolvingDrawsThreeThenPromptsForDiscard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Hand should have 3 cards (spell left hand, then drew 3)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Deck should have lost 3 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        // Should be awaiting discard choice
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.awaitingCardChoicePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Completing discard results in net gain of two cards")
    void completingDiscardResultsInNetGainOfTwo() {
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Discard the first card
        harness.handleCardChosen(player1, 0);

        // Hand should have 2 cards (drew 3, discarded 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // No longer awaiting input
        assertThat(gd.awaitingInput).isNull();
    }

    @Test
    @DisplayName("Sift goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sift"));
    }

    @Test
    @DisplayName("Can choose which card to discard")
    void canChooseWhichCardToDiscard() {
        harness.setHand(player1, List.of(new Sift()));
        setDeck(player1, List.of(new GrizzlyBears(), new Forest(), new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Hand should have [GrizzlyBears, Forest, Sift], discard the last one (index 2)
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sift") && c != gd.playerGraveyards.get(player1.getId()).getFirst());
    }

    // ===== Helpers =====

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
