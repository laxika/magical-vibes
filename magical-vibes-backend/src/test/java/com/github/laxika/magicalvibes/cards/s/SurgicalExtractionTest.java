package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurgicalExtractionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Surgical Extraction has correct card properties")
    void hasCorrectProperties() {
        SurgicalExtraction card = new SurgicalExtraction();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ExileTargetGraveyardCardAndSameNameFromZonesEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a graveyard card")
    void castingPutsItOnStack() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Surgical Extraction");
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolution flow =====

    @Test
    @DisplayName("Resolving prompts for card selection when matching cards exist")
    void resolvingPromptsForCardSelection() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_ZONE_EXILE_CHOICE);
    }

    // ===== Exile behavior =====

    @Test
    @DisplayName("Exiles matching cards from target player's hand")
    void exilesMatchingCardsFromHand() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card peek = new Peek();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2, peek)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        // Select all matching cards to exile
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        // Both Grizzly Bears should be exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);

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
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1, bears2)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles matching cards from target player's library")
    void exilesMatchingCardsFromLibrary() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, List.of());
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears2);

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
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

        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2, peek)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears3);

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

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
    @DisplayName("No matching cards in other zones just shuffles library")
    void noMatchingCardsInOtherZonesShufflesLibrary() {
        Card bears = new GrizzlyBears();
        Card peek = new Peek();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, new ArrayList<>(List.of(peek)));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        // Only the targeted card is in the graveyard — select it
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        // Bears exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Library size unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

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

        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears3);

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        // Only select the one from graveyard — leave hand and library copies
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId()));

        // Only 1 card exiled
        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(exiledCount).isEqualTo(1);

        // Graveyard copy gone
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears1.getId()));

        // Hand copy still there
        assertThat(gd.playerHands.get(player2.getId()))
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

        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        // Select zero cards
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Both copies remain in their zones
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears1.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));

        // Log should mention 0 cards exiled and library shuffled
        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles 0 cards"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target card in own graveyard")
    void canTargetOwnGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        // Bears exiled from player1's graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a basic land card in graveyard")
    void cannotTargetBasicLand() {
        Card plains = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(plains)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("basic land");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Surgical Extraction goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Surgical Extraction"));
    }

    // ===== Library shuffle =====

    @Test
    @DisplayName("Library is shuffled after resolution")
    void libraryIsShuffledAfterResolution() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        // Log should mention shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    // ===== Logging =====

    @Test
    @DisplayName("Exile count is logged")
    void exileCountIsLogged() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears1.getId());
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles 2 cards"));
    }
}
