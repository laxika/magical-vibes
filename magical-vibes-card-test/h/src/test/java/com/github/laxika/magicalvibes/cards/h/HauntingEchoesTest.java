package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CabalPit;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.m.MindlockOrb;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingEchoes.class, DuskImp.class, Plains.class, CabalPit.class, MindlockOrb.class})
class HauntingEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Search prohibition prevents library exile but not graveyard exile or shuffle")
    void searchProhibitionLeavesLibraryCardsAlone() {
        Card graveyardImp = new DuskImp();
        Card libraryImp = new DuskImp();
        harness.addToBattlefield(player2, new MindlockOrb());
        harness.setGraveyard(player2, List.of(graveyardImp));
        harness.setLibrary(player2, List.of(libraryImp));
        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardImp);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryImp);
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("Caster chooses which matching library cards to exile")
    void matchingLibraryCardsRequireCasterChoice() {
        Card graveyardImp = new DuskImp();
        Card firstLibraryImp = new DuskImp();
        Card secondLibraryImp = new DuskImp();
        harness.setGraveyard(player2, List.of(graveyardImp));
        harness.setLibrary(player2, List.of(firstLibraryImp, secondLibraryImp));
        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardImp);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId()))
                .contains(firstLibraryImp, secondLibraryImp);
    }

    @Test
    @DisplayName("Basic land names do not cause library copies to be exiled")
    void basicLandNameIsExcludedFromLibrarySearch() {
        Card graveyardPlains = new Plains();
        Card libraryPlains = new Plains();
        harness.setGraveyard(player2, List.of(new DuskImp(), graveyardPlains));
        harness.setLibrary(player2, List.of(libraryPlains));
        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardPlains);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryPlains);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Self-targeting does not count the resolving spell as a graveyard card")
    void resolvingSpellDoesNotExileItselfOrLibraryCopies() {
        Card spell = new HauntingEchoes();
        Card libraryCopy = new HauntingEchoes();
        harness.setGraveyard(player1, List.of(new DuskImp()));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Haunting Echoes"));
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
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Resolution: graveyard exile =====

    @Test
    @DisplayName("Exiles non-basic-land cards from target player's graveyard")
    void exilesNonBasicLandCardsFromGraveyard() {
        Card imp1 = new DuskImp();
        Card imp2 = new DuskImp();
        harness.setGraveyard(player2, List.of(imp1, imp2));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotInGraveyard(player2, "Dusk Imp");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dusk Imp"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Basic land cards remain in graveyard")
    void basicLandCardsRemainInGraveyard() {
        Card imp = new DuskImp();
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(imp, plains));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Dusk Imp exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dusk Imp"));

        // Plains stays in graveyard
        harness.assertInGraveyard(player2, "Plains");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Plains"));
    }

    // ===== Resolution: library search =====

    @Test
    @DisplayName("Exiles matching cards from target player's library")
    void exilesMatchingCardsFromLibrary() {
        Card imp1 = new DuskImp();
        Card imp2 = new DuskImp();
        harness.setGraveyard(player2, List.of(imp1));
        harness.setLibrary(player2, List.of(imp2));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Both should be exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dusk Imp"))
                .hasSize(2);

        // Library should not contain Dusk Imp
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Dusk Imp"));
    }

    @Test
    @DisplayName("Does not exile library cards whose names were not in the graveyard")
    void doesNotExileUnrelatedLibraryCards() {
        Card imp = new DuskImp();
        harness.setGraveyard(player2, List.of(imp));

        // Put unrelated cards in library
        harness.setLibrary(player2, List.of(new Plains()));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Plains should remain in library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    // ===== Empty / only basic lands graveyard =====

    @Test
    @DisplayName("Empty graveyard resolves with just shuffle")
    void emptyGraveyardJustShuffles() {
        harness.setGraveyard(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        // Library size unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

        // Log mentions shuffle
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("Graveyard with only basic lands results in no exiles")
    void graveyardWithOnlyBasicLandsNoExiles() {
        Card plains1 = new Plains();
        Card plains2 = new Plains();
        harness.setGraveyard(player2, List.of(plains1, plains2));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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
        Card imp1 = new DuskImp();
        Card pit1 = new CabalPit();
        Card plains = new Plains();
        // Both names are intentionally different so each graveyard name is searched.
        Card imp2 = new DuskImp();
        Card pit2 = new CabalPit();

        harness.setGraveyard(player2, List.of(imp1, pit1));
        harness.setLibrary(player2, List.of(imp2, pit2, plains));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Both names are exiled from the graveyard and library.
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dusk Imp"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Cabal Pit"))
                .hasSize(2);

        // Plains stays in library (it wasn't in the graveyard as a non-basic-land card)
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Exiles nonbasic land cards from the graveyard")
    void exilesNonBasicLandCardFromGraveyard() {
        Card pit1 = new CabalPit();
        Card pit2 = new CabalPit();
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(pit1));
        harness.setLibrary(player2, List.of(pit2, plains));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Cabal Pit"))
                .hasSize(2);
        harness.assertNotInGraveyard(player2, "Cabal Pit");
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(plains);
    }

    // ===== Library shuffle =====

    @Test
    @DisplayName("Library is shuffled after resolution")
    void libraryIsShuffledAfterResolution() {
        Card imp = new DuskImp();
        harness.setGraveyard(player2, List.of(imp));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card imp = new DuskImp();
        harness.setGraveyard(player1, List.of(imp));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dusk Imp"));
        harness.assertNotInGraveyard(player1, "Dusk Imp");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Haunting Echoes goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Haunting Echoes");
    }

    // ===== Logging =====

    @Test
    @DisplayName("Exile counts are logged")
    void exileCountsAreLogged() {
        Card imp1 = new DuskImp();
        Card imp2 = new DuskImp();
        harness.setGraveyard(player2, List.of(imp1));
        harness.setLibrary(player2, List.of(imp2));

        harness.setHand(player1, List.of(new HauntingEchoes()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Should log exile counts from graveyard and library
        assertThat(gameLogContains("exiles 1 card from")).isTrue();
        assertThat(gameLogContains("and 1 card from their library")).isTrue();
    }
}
