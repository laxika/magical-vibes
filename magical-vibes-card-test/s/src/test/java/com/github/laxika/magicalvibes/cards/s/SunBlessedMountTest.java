package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.h.HuatliDinosaurKnight;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunBlessedMount.class, HuatliDinosaurKnight.class})
class SunBlessedMountTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sun-Blessed Mount triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Huatli, Dinosaur Knight in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card huatli = createHuatliDinosaurKnight();
        harness.setGraveyard(player1, List.of(huatli));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Huatli, Dinosaur Knight");
        harness.assertNotInGraveyard(player1, "Huatli, Dinosaur Knight");
    }

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card huatli = createHuatliDinosaurKnight();
        harness.setLibrary(player1, List.of(huatli));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        // Library search prompt should appear
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Huatli, Dinosaur Knight");
    }

    @Test
    @DisplayName("Accepting may when Huatli is not in library or graveyard does nothing")
    void acceptingMayWhenNotFound() {
        harness.setLibrary(player1, List.of());
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may ability does not search")
    void decliningMayDoesNotSearch() {
        Card huatli = createHuatliDinosaurKnight();
        harness.setGraveyard(player1, List.of(huatli));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        // Huatli stays in graveyard
        harness.assertInGraveyard(player1, "Huatli, Dinosaur Knight");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sun-Blessed Mount enters the battlefield after resolving")
    void sunBlessedMountEntersBattlefield() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Sun-Blessed Mount");
    }

    @Test
    @DisplayName("A selected library Huatli is put into hand and removed from the library")
    void selectedLibraryCardGoesToHand() {
        Card huatli = new HuatliDinosaurKnight();
        harness.setLibrary(player1, List.of(huatli));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(huatli);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library search may fail to find even when Huatli is present")
    void librarySearchMayFailToFind() {
        Card huatli = new HuatliDinosaurKnight();
        harness.setLibrary(player1, List.of(huatli));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(huatli);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search does not force a graveyard copy when a library copy is available")
    void playerChoosesBetweenLibraryAndGraveyardCopies() {
        Card graveyardHuatli = new HuatliDinosaurKnight();
        Card libraryHuatli = new HuatliDinosaurKnight();
        harness.setGraveyard(player1, List.of(graveyardHuatli));
        harness.setLibrary(player1, List.of(libraryHuatli));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardHuatli);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryHuatli);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("The controller chooses which matching graveyard copy to return")
    void playerChoosesBetweenGraveyardCopies() {
        Card firstHuatli = new HuatliDinosaurKnight();
        Card secondHuatli = new HuatliDinosaurKnight();
        harness.setGraveyard(player1, List.of(firstHuatli, secondHuatli));
        harness.setLibrary(player1, List.of());
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstHuatli, secondHuatli);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new SunBlessedMount(), "{3}{R}{W}");
    }

    private Card createHuatliDinosaurKnight() {
        return new HuatliDinosaurKnight();
    }
}
