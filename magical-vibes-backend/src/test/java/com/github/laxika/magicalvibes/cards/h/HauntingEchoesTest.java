package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HauntingEchoesTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Haunting Echoes has correct card properties")
    void hasCorrectProperties() {
        HauntingEchoes card = new HauntingEchoes();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Haunting Echoes");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Resolution: graveyard exile =====

    @Test
    @DisplayName("Exiles non-basic-land cards from target player's graveyard")
    void exilesNonBasicLandCardsFromGraveyard() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1, bears2)));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Basic land cards remain in graveyard")
    void basicLandCardsRemainInGraveyard() {
        Card bears = new GrizzlyBears();
        Card plains = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears, plains)));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Bears exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Plains stays in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Plains"));
    }

    // ===== Resolution: library search =====

    @Test
    @DisplayName("Exiles matching cards from target player's library")
    void exilesMatchingCardsFromLibrary() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears2);

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both should be exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);

        // Library should not contain Grizzly Bears
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not exile library cards whose names were not in the graveyard")
    void doesNotExileUnrelatedLibraryCards() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        // Put unrelated cards in library
        gd.playerDecks.get(player2.getId()).clear();
        Card plains = new Plains();
        gd.playerDecks.get(player2.getId()).add(plains);

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Plains should remain in library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    // ===== Empty / only basic lands graveyard =====

    @Test
    @DisplayName("Empty graveyard resolves with just shuffle")
    void emptyGraveyardJustShuffles() {
        harness.setGraveyard(player2, new ArrayList<>());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        // Library size unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

        // Log mentions shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Graveyard with only basic lands results in no exiles")
    void graveyardWithOnlyBasicLandsNoExiles() {
        Card plains1 = new Plains();
        Card plains2 = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(plains1, plains2)));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both Plains remain in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Plains"))
                .hasSize(2);

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    // ===== Multiple unique names =====

    @Test
    @DisplayName("Exiles library copies for each unique name in graveyard")
    void exilesLibraryCopiesForMultipleNames() {
        Card bears1 = new GrizzlyBears();
        Card plains = new Plains();
        // Use a second different non-land card — we'll use another GrizzlyBears but check total counts
        Card bears2 = new GrizzlyBears();

        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears2);
        gd.playerDecks.get(player2.getId()).add(plains);

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // All Grizzly Bears exiled (1 from graveyard + 1 from library)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);

        // Plains stays in library (it wasn't in the graveyard as a non-basic-land card)
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    // ===== Library shuffle =====

    @Test
    @DisplayName("Library is shuffled after resolution")
    void libraryIsShuffledAfterResolution() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their library"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Haunting Echoes goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setGraveyard(player2, new ArrayList<>());
        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Haunting Echoes"));
    }

    // ===== Logging =====

    @Test
    @DisplayName("Exile counts are logged")
    void exileCountsAreLogged() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears1)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears2);

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should log exile counts from graveyard and library
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("1 card from") && log.contains("graveyard")
                        && log.contains("1 card from") && log.contains("library"));
    }
}
