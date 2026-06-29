package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlueSunsZenithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Blue Sun's Zenith has correct effects")
    void hasCorrectEffects() {
        BlueSunsZenith card = new BlueSunsZenith();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawXCardsForTargetPlayerEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

    @Test
    @DisplayName("Blue Sun's Zenith targets a player")
    void targetsPlayer() {
        assertThat(EffectResolution.needsTarget(new BlueSunsZenith())).isTrue();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BlueSunsZenith()));
        harness.addMana(player1, ManaColor.BLUE, 6); // X=3: {3}{U}{U}{U} = 6

        harness.castInstant(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Blue Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution: draw cards =====

    @Test
    @DisplayName("X=3 draws 3 cards for target player")
    void xEqualsThreeDrawsThreeCards() {
        harness.setHand(player1, List.of(new BlueSunsZenith()));
        harness.addMana(player1, ManaColor.BLUE, 6); // X=3: {3}{U}{U}{U} = 6
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("X=0 draws no cards")
    void xZeroDrawsNoCards() {
        harness.setHand(player1, List.of(new BlueSunsZenith()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=0: {0}{U}{U}{U} = 3
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Can target yourself to draw cards")
    void canTargetYourself() {
        harness.setHand(player1, List.of(new BlueSunsZenith()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2: {2}{U}{U}{U} = 5
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castInstant(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Blue Sun's Zenith is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setHand(player1, List.of(new BlueSunsZenith()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1: {1}{U}{U}{U} = 4

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blue Sun's Zenith"));
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blue Sun's Zenith"));
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new BlueSunsZenith()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
