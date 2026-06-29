package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateXCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhiteSunsZenithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("White Sun's Zenith has correct effects")
    void hasCorrectEffects() {
        WhiteSunsZenith card = new WhiteSunsZenith();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CreateXCreatureTokenEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 6); // X=3: {3}{W}{W}{W} = 6

        harness.castInstant(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("White Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution: token creation =====

    @Test
    @DisplayName("X=3 creates 3 Cat tokens")
    void xEqualsThreeCreatesThreeCatTokens() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 6); // X=3: {3}{W}{W}{W} = 6

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        List<Permanent> catTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .toList();
        assertThat(catTokens).hasSize(3);
    }

    @Test
    @DisplayName("Cat tokens are 2/2 white Cats")
    void catTokensHaveCorrectProperties() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 4); // X=1: {1}{W}{W}{W} = 4

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent catToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .findFirst().orElseThrow();

        assertThat(catToken.getCard().getPower()).isEqualTo(2);
        assertThat(catToken.getCard().getToughness()).isEqualTo(2);
        assertThat(catToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(catToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(catToken.getCard().getSubtypes()).contains(CardSubtype.CAT);
    }

    @Test
    @DisplayName("X=0 creates no tokens")
    void xZeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 3); // X=0: {0}{W}{W}{W} = 3

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        long catTokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .count();
        assertThat(catTokenCount).isZero();
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("White Sun's Zenith is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 4); // X=1: {1}{W}{W}{W} = 4

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("White Sun's Zenith"));
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("White Sun's Zenith"));
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 4); // X=1

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
