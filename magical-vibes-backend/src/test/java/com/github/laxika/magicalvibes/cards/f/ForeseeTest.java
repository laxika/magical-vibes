package com.github.laxika.magicalvibes.cards.f;

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

class ForeseeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Foresee has scry 4 and draw 2 spell effects")
    void hasCorrectProperties() {
        Foresee card = new Foresee();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ScryEffect.class);
        ScryEffect scryEffect = (ScryEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(scryEffect.count()).isEqualTo(4);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(drawEffect.amount()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Foresee puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Foresee");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving Foresee enters scry state with 4 cards")
    void resolvingEntersScryState() {
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(4);
    }

    @Test
    @DisplayName("After scry completes, draws two cards")
    void afterScryDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Complete scry by keeping all on top
        gs.handleScryCompleted(gd, player1, List.of(0, 1, 2, 3), List.of());

        // Hand should have 2 cards (spell left hand, then drew 2)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // Deck should have lost 2 cards from draws
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Scry reorders top of library before draw")
    void scryReordersBeforeDraw() {
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Reverse the top 4, then draw 2 — should draw what was originally at positions 3 and 2
        gs.handleScryCompleted(gd, player1, List.of(3, 2, 1, 0), List.of());

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(2);
        assertThat(hand.get(0)).isSameAs(top3);
        assertThat(hand.get(1)).isSameAs(top2);
    }

    @Test
    @DisplayName("Scry putting cards on bottom then drawing")
    void scryBottomThenDraw() {
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);
        Card top4 = deck.get(4);
        Card top5 = deck.get(5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Put all 4 on bottom, then draw 2 — should draw what was originally at positions 4 and 5
        gs.handleScryCompleted(gd, player1, List.of(), List.of(0, 1, 2, 3));

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(2);
        assertThat(hand.get(0)).isSameAs(top4);
        assertThat(hand.get(1)).isSameAs(top5);
    }

    @Test
    @DisplayName("Foresee goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Complete scry
        gs.handleScryCompleted(gd, player1, List.of(0, 1, 2, 3), List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Foresee"));
    }
}
