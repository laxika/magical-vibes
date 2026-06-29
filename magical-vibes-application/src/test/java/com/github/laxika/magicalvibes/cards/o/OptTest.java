package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Opt has scry 1 and draw 1 spell effects")
    void hasCorrectProperties() {
        Opt card = new Opt();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ScryEffect.class);
        ScryEffect scryEffect = (ScryEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(scryEffect.count()).isEqualTo(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(drawEffect.amount()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Opt puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Opt");
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
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);
    }

    @Test
    @DisplayName("After scry completes, draws one card")
    void afterScryDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Complete scry by keeping the card on top
        gs.handleScryCompleted(gd, player1, List.of(0), List.of());

        // Hand should have 1 card (spell left hand, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should have lost 1 card from draw
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Scry putting card on bottom then drawing")
    void scryBottomThenDraw() {
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Put the top card on bottom, then draw 1 — should draw what was originally at position 1
        gs.handleScryCompleted(gd, player1, List.of(), List.of(0));

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top1);
    }

    @Test
    @DisplayName("Opt goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Complete scry
        gs.handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Opt"));
    }
}
