package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesperateRavingsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Desperate Ravings has correct card properties")
    void hasCorrectProperties() {
        DesperateRavings card = new DesperateRavings();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(drawEffect.amount()).isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(TargetPlayerRandomDiscardEffect.class);
        TargetPlayerRandomDiscardEffect discardEffect = (TargetPlayerRandomDiscardEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(discardEffect.amount()).isEqualTo(1);
        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{2}{U}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Desperate Ravings puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Desperate Ravings");
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving draws two cards then discards one at random")
    void resolvingDrawsTwoThenDiscardsOneAtRandom() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Spell left hand (-1), drew 2, discarded 1 at random = net 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck lost 2 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        // Graveyard has Desperate Ravings + 1 discarded
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        // Random discard doesn't prompt
        assertThat(gd.interaction.awaitingInputType()).isNull();
        // Log should mention discard at random
        long randomDiscardLogs = gd.gameLog.stream()
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(1);
    }

    @Test
    @DisplayName("Desperate Ravings goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Desperate Ravings"));
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback from graveyard draws two and discards one at random")
    void flashbackDrawsTwoAndDiscardsOne() {
        harness.setHand(player1, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setGraveyard(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Drew 2, discarded 1 at random = 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Desperate Ravings"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Desperate Ravings"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsInstant() {
        harness.setGraveyard(player1, List.of(new DesperateRavings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Desperate Ravings");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new DesperateRavings()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
