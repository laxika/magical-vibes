package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.PhyrexiasCore;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgicalExtraction.class, GrizzlyBears.class, Peek.class, Plains.class, PhyrexiasCore.class})
class SurgicalExtractionTest extends BaseCardTest {

    @Test
    void canTargetNonbasicLand() {
        Card core = new PhyrexiasCore();
        harness.setGraveyard(player2, List.of(core));
        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, core.getId());
        harness.handleMultipleCardsChosen(player1, List.of(core.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(core);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(core);
    }

    @Test
    void canPayTwoLifeWithoutBlackMana() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertLife(player1, 18);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        harness.assertInGraveyard(player1, "Surgical Extraction");
    }

    @Test
    void canLeaveTargetInGraveyardAndExileOnlyHandCopy() {
        Card target = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card casterCopy = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player1, List.of(casterCopy));
        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(casterCopy);
        harness.assertLife(player1, 20);
    }

    @Test
    void removedTargetPreventsSearchExileAndShuffle() {
        Card target = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player2, List.of(handCopy));
        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of());
        gd.addToExile(player2.getId(), target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("shuffles their library"));
        harness.assertInGraveyard(player1, "Surgical Extraction");
    }

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

    @Test
    @DisplayName("Resolving prompts for card selection when matching cards exist")
    void resolvingPromptsForCardSelection() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
    }

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

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        // Select all matching cards to exile
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        // Both Grizzly Bears should be exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);

        // Grizzly Bears should not be in hand
        harness.assertNotInHand(player2, "Grizzly Bears");

        // Peek should remain in hand
        harness.assertInHand(player2, "Peek");
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

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles matching cards from target player's library")
    void exilesMatchingCardsFromLibrary() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(bears2));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

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
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        // All 3 copies should be exiled
        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        // No Grizzly Bears in any zone
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Peek should remain in hand
        harness.assertInHand(player2, "Peek");
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

        harness.castAndResolveInstant(player1, 0, bears.getId());

        // Only the targeted card is in the graveyard — select it
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // Bears exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Library size unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

        // Log should mention shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Partial selection: only selected cards are exiled")
    void partialSelectionOnlyExilesSelected() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();

        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        // Only select the one from graveyard — leave hand and library copies
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId()));

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Zero selection: no cards exiled but library is shuffled")
    void zeroSelectionNoCardsExiled() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();

        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        // Select zero cards
        harness.handleMultipleCardsChosen(player1, List.of());

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Both copies remain in their zones
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears1.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));

        // Log should mention 0 cards exiled and library shuffled
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 0 cards"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Can target card in own graveyard")
    void canTargetOwnGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // Bears exiled from player1's graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a basic land card in graveyard")
    void cannotTargetBasicLand() {
        Card plains = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(plains)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(plains);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Surgical Extraction goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Surgical Extraction");
    }

    @Test
    @DisplayName("Library is shuffled after resolution")
    void libraryIsShuffledAfterResolution() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // Log should mention shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Exile count is logged")
    void exileCountIsLogged() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        harness.setHand(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new SurgicalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 2 cards"));
    }
}
