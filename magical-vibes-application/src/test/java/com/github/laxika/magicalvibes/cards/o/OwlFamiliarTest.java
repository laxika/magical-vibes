package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OwlFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card, then discards a card (net hand size unchanged)")
    void etbDrawThenDiscard() {
        setDeck(player1, List.of(new Forest()));
        castOwlFamiliar();

        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve ETB trigger — draws, then prompts discard

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        // Drew 1 (Forest), discarded 1 → hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature enters the battlefield")
    void creatureEntersBattlefield() {
        setDeck(player1, List.of(new Forest()));
        castOwlFamiliar();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Owl Familiar"));
    }

    private void castOwlFamiliar() {
        harness.setHand(player1, List.of(new OwlFamiliar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
