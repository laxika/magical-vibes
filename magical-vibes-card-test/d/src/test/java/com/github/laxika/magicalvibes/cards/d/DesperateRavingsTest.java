package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesperateRavings.class})
class DesperateRavingsTest extends BaseCardTest {

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

        harness.castAndResolveInstant(player1, 0);

        // Spell left hand (-1), drew 2, discarded 1 at random = net 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck lost 2 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        // Graveyard has Desperate Ravings + 1 discarded
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        // Random discard doesn't prompt
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Log should mention discard at random
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
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

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Desperate Ravings");
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

        harness.castAndResolveFlashback(player1, 0, null);

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

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Desperate Ravings");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
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
    @Test
    @DisplayName("Draw and random discard affect the controller on the opponent's turn")
    void affectsControllerOnOpponentsTurn() {
        DesperateRavings spell = new DesperateRavings();
        DesperateRavings firstDraw = new DesperateRavings();
        DesperateRavings secondDraw = new DesperateRavings();
        DesperateRavings remaining = new DesperateRavings();
        DesperateRavings opponentsCard = new DesperateRavings();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(opponentsCard));
        harness.setHand(player2, List.of(spell));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, remaining));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1)
                .allMatch(card -> card == firstDraw || card == secondDraw);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2).contains(spell);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card != spell).toList()).hasSize(1)
                .allMatch(card -> card == firstDraw || card == secondDraw);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A normally resolved card can be cast again with flashback and is then exiled")
    void normalCastThenFlashback() {
        DesperateRavings spell = new DesperateRavings();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        int spellIndex = gd.playerGraveyards.get(player1.getId()).indexOf(spell);
        assertThat(spellIndex).isNotNegative();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveFlashback(player1, spellIndex, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).doesNotContain(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
