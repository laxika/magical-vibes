package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RitualOfRejuvenationTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ritual of Rejuvenation has correct spell effects")
    void hasCorrectEffects() {
        RitualOfRejuvenation card = new RitualOfRejuvenation();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount()).isEqualTo(4);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Ritual of Rejuvenation puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new RitualOfRejuvenation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ritual of Rejuvenation");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Ritual of Rejuvenation gains 4 life for its controller")
    void gains4Life() {
        harness.setLife(player1, 16);
        harness.setHand(player1, List.of(new RitualOfRejuvenation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ritual of Rejuvenation draws a card for its controller")
    void drawsACard() {
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RitualOfRejuvenation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Hand had 1 card (the spell), cast it (0 cards), then drew 1 card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's life total is unaffected")
    void opponentLifeUnaffected() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RitualOfRejuvenation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Graveyard =====

    @Test
    @DisplayName("Ritual of Rejuvenation goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new RitualOfRejuvenation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ritual of Rejuvenation"));
    }
}
