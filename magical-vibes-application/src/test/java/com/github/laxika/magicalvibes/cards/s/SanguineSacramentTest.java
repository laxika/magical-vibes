package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeMultipliedByXValueEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanguineSacramentTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has GainLifeMultipliedByXValueEffect with multiplier 2 and PutSelfOnBottomOfOwnersLibraryEffect")
    void hasCorrectEffects() {
        SanguineSacrament card = new SanguineSacrament();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(GainLifeMultipliedByXValueEffect.class);
        assertThat(((GainLifeMultipliedByXValueEffect) card.getEffects(EffectSlot.SPELL).get(0)).multiplier())
                .isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(PutSelfOnBottomOfOwnersLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sanguine Sacrament");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution — life gain =====

    @Test
    @DisplayName("Resolving gains twice X life (X=3 gains 6)")
    void gainsTwiceXLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("X=0 gains no life")
    void xZeroGainsNoLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("X=5 gains 10 life")
    void xFiveGainsTenLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castInstant(player1, 0, 5, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Resolution — library disposition =====

    @Test
    @DisplayName("Goes to bottom of owner's library after resolving, not graveyard")
    void goesToBottomOfLibraryAfterResolving() {
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Sanguine Sacrament"));
        // On the bottom of the library (last element in the deck list)
        List<?> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(((com.github.laxika.magicalvibes.model.Card) deck.getLast()).getName())
                .isEqualTo("Sanguine Sacrament");
    }

    @Test
    @DisplayName("Not in exile after resolving")
    void notInExileAfterResolving() {
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getName().equals("Sanguine Sacrament"));
    }

    @Test
    @DisplayName("Can pay X with any color mana beyond the WW base cost")
    void canPayXWithAnyColor() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }
}
