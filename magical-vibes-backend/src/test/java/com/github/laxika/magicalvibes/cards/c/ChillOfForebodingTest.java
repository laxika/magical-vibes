package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChillOfForebodingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has two SPELL effects: mill controller 5 and each opponent mills 5")
    void hasCorrectEffects() {
        ChillOfForeboding card = new ChillOfForeboding();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(MillControllerEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(EachOpponentMillsEffect.class);

        MillControllerEffect millController = (MillControllerEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(millController.count()).isEqualTo(5);

        EachOpponentMillsEffect millOpponent = (EachOpponentMillsEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(millOpponent.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("Has flashback cost {7}{U}")
    void hasFlashbackCost() {
        ChillOfForeboding card = new ChillOfForeboding();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{7}{U}");
    }

    // ===== Casting normally =====

    @Test
    @DisplayName("Both players mill five cards when cast")
    void bothPlayersMillFive() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 5);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5 + 1); // 5 milled + the spell itself
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Spell goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chill of Foreboding"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not crash when caster's library has fewer than five cards")
    void doesNotCrashWhenCasterLibrarySmall() {
        // Leave only 2 cards in caster's library
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 2) {
            deck.removeLast();
        }
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2 + 1); // 2 milled + the spell
        // Opponent should still mill 5
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 5);
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback mills both players five cards")
    void flashbackMillsBothPlayers() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 5);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Chill of Foreboding"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chill of Foreboding"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as sorcery spell")
    void flashbackPutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Chill of Foreboding");
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        // Only 1 blue mana, but flashback costs {7}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
