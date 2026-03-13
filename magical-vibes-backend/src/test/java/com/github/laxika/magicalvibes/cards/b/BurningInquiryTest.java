package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurningInquiryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Burning Inquiry has correct effects")
    void hasCorrectEffects() {
        BurningInquiry card = new BurningInquiry();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(EachPlayerDrawsCardEffect.class);
        EachPlayerDrawsCardEffect drawEffect = (EachPlayerDrawsCardEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(drawEffect.amount()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(EachPlayerRandomDiscardEffect.class);
        EachPlayerRandomDiscardEffect discardEffect = (EachPlayerRandomDiscardEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(discardEffect.amount()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Burning Inquiry puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Burning Inquiry");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BurningInquiry()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving makes each player draw 3 then discard 3 at random")
    void resolvingDrawsAndDiscardsForEachPlayer() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player 1: cast spell (-1 card from hand), drew 3, discarded 3 at random = 0 cards in hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Player 1 deck lost 3 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 3);
        // Player 1 graveyard: Burning Inquiry + 3 discarded = 4
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);

        // Player 2: started with empty hand, drew 3, discarded 3 at random = 0 cards in hand
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Player 2 deck lost 3 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);
        // Player 2 graveyard: 3 discarded
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);

        // Should NOT be awaiting any input (random discard doesn't prompt)
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Log shows random discard for each player")
    void logShowsRandomDiscardForEachPlayer() {
        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        long randomDiscardLogs = gd.gameLog.stream()
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        // 3 discards for player1 + 3 discards for player2 = 6
        assertThat(randomDiscardLogs).isEqualTo(6);
    }

    @Test
    @DisplayName("Burning Inquiry goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Burning Inquiry"));
    }

    @Test
    @DisplayName("When a player has fewer cards than 3 after drawing, discards all available")
    void discardsAllWhenFewerThanThreeCardsAfterDraw() {
        // Give player2 a very small deck so they draw fewer than 3
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player 2 drew only 1 card (deck ran out), discards 3 at random but only 1 available
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Player 2 graveyard has 1 discarded card
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
