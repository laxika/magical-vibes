package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoricideTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Memoricide has correct card properties")
    void hasCorrectProperties() {
        Memoricide card = new Memoricide();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ChooseCardNameAndExileFromZonesEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Memoricide");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolution flow =====

    @Test
    @DisplayName("Resolving prompts caster for a card name choice")
    void resolvingPromptsForCardNameChoice() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("After name choice with matches, prompts for card selection")
    void afterNameChoicePromptsForCardSelection() {
        Card bears = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_ZONE_EXILE_CHOICE);
    }

    // ===== Exile behavior =====

    @Test
    @DisplayName("Exiles matching cards from target player's hand")
    void exilesMatchingCardsFromHand() {
        Card bears1 = new GrizzlyBears();
        Card peek = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose "Grizzly Bears"
        harness.handleListChoice(player1, "Grizzly Bears");

        // Select all matching cards to exile
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId()));

        // Grizzly Bears should be exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Grizzly Bears should not be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Peek should remain in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Exiles matching cards from target player's graveyard")
    void exilesMatchingCardsFromGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles matching cards from target player's library")
    void exilesMatchingCardsFromLibrary() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles matching cards from all three zones at once")
    void exilesMatchingCardsFromAllZones() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        Card peek = new Peek();

        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears3);

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        // All 3 copies should be exiled
        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        // No Grizzly Bears in any zone
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Peek should remain in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Choosing a name with no matching cards just shuffles library")
    void noMatchingCardsResultsInNoExile() {
        Card peek = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(peek)));
        harness.setGraveyard(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        // No card selection step — resolves immediately when no matches
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_ZONE_EXILE_CHOICE);

        // No cards exiled (no Grizzly Bears in any zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Library size unchanged (just shuffled)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

        // Log should mention 0 cards exiled
        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles 0 cards"));
    }

    @Test
    @DisplayName("Library is shuffled after resolution")
    void libraryIsShuffledAfterResolution() {
        // Fill library with predictable cards
        gd.playerDecks.get(player2.getId()).clear();
        List<Card> originalOrder = new ArrayList<>();
        List<UUID> bearsIds = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Card bears = new GrizzlyBears();
            originalOrder.add(bears);
            bearsIds.add(bears.getId());
        }
        originalOrder.add(new Peek());
        gd.playerDecks.get(player2.getId()).addAll(originalOrder);
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleGraveyardCardsChosen(player1, bearsIds);

        // All Grizzly Bears exiled, only Peek should remain
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Peek");

        // Log should mention shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    // ===== Partial selection =====

    @Test
    @DisplayName("Partial selection: only selected cards are exiled")
    void partialSelectionOnlyExilesSelected() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();

        harness.setHand(player2, new ArrayList<>(List.of(bears1)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears3);

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        // Only select the one from hand — leave graveyard and library copies
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId()));

        // Only 1 card exiled
        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(exiledCount).isEqualTo(1);

        // Hand copy gone
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Graveyard copy still there
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));

        // Library copy still there
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears3.getId()));

        // Library still shuffled
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    // ===== Zero selection =====

    @Test
    @DisplayName("Zero selection: no cards exiled but library is shuffled")
    void zeroSelectionNoCardsExiled() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();

        harness.setHand(player2, new ArrayList<>(List.of(bears1)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        // Select zero cards
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Both copies remain in their zones
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears1.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));

        // Log should mention 0 cards exiled and library shuffled
        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles 0 cards"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new Memoricide(), bears)));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        // Grizzly Bears exiled from player1's hand
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Memoricide goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No matching cards — resolves immediately without selection step
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memoricide"));
    }

    // ===== Logging =====

    @Test
    @DisplayName("Name choice is logged")
    void nameChoiceIsLogged() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses") && log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exile count is logged")
    void exileCountIsLogged() {
        Card bears = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles 1 card"));
    }
}
