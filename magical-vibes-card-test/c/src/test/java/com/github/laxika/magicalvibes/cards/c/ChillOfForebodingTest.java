package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChillOfForeboding.class})
class ChillOfForebodingTest extends BaseCardTest {

    @Test
    @DisplayName("Flashback requires all seven generic mana in addition to blue")
    void flashbackRequiresSevenGenericMana() {
        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Chill of Foreboding");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both players mill five cards when cast")
    void bothPlayersMillFive() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Chill of Foreboding");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not crash when caster's library has fewer than five cards")
    void doesNotCrashWhenCasterLibrarySmall() {
        harness.setLibrary(player1, gd.playerDecks.get(player1.getId()).subList(0, 2));
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2 + 1); // 2 milled + the spell
        // Opponent should still mill 5
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 5);
    }

    @Test
    @DisplayName("Flashback mills both players five cards")
    void flashbackMillsBothPlayers() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 5);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Chill of Foreboding");
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

    @Test
    @DisplayName("Opponent with fewer than five cards mills the remaining library")
    void millsAllOfShortOpponentLibrary() {
        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).subList(0, 2));
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 5);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Flashback resolves and exiles the spell with both libraries empty")
    void flashbackResolvesWithEmptyLibraries() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        ChillOfForeboding spell = new ChillOfForeboding();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot pay its blue requirement with colorless mana")
    void flashbackRequiresBlueMana() {
        harness.setGraveyard(player1, List.of(new ChillOfForeboding()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Chill of Foreboding");
        assertThat(gd.stack).isEmpty();
    }

}
