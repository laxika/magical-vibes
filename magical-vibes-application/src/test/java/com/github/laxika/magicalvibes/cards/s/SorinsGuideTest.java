package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SorinsGuide.class, SorinVampireLord.class})
class SorinsGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sorin's Guide triggers a may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Sorin, Vampire Lord in the graveyard")
    void acceptingMayFindsInGraveyard() {
        Card sorin = createSorinVampireLord();
        harness.setGraveyard(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(true);

        harness.assertInHand(player1, "Sorin, Vampire Lord");
        harness.assertNotInGraveyard(player1, "Sorin, Vampire Lord");
    }

    @Test
    @DisplayName("Accepting may searches the library when Sorin is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        Card sorin = createSorinVampireLord();
        harness.setLibrary(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Sorin, Vampire Lord");
    }

    @Test
    @DisplayName("Declining may leaves Sorin in the graveyard")
    void decliningMayDoesNotSearch() {
        Card sorin = createSorinVampireLord();
        harness.setGraveyard(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(false);

        harness.assertInGraveyard(player1, "Sorin, Vampire Lord");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting may when Sorin is absent from both zones finds nothing")
    void acceptingMayWhenNotFound() {
        harness.setLibrary(player1, List.of());
        setupAndCast();

        resolveMayPrompt(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new SorinsGuide(), "{3}{B}{B}");
    }

    private void resolveMayPrompt(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Card createSorinVampireLord() {
        return new SorinVampireLord();
    }

    @Test
    @DisplayName("Choosing Sorin from the library puts the chosen card into hand")
    void librarySelectionMovesCardToHand() {
        Card sorin = new SorinVampireLord();
        harness.setLibrary(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorin);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sorin);
    }

    @Test
    @DisplayName("A library search may fail to find even when Sorin is present")
    void librarySearchMayFailToFind() {
        Card sorin = new SorinVampireLord();
        harness.setLibrary(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Sorin, Vampire Lord");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorin);
    }

    @Test
    @DisplayName("A graveyard copy must not force the controller to take it instead of searching the library")
    void graveyardCopyDoesNotRemoveSearchChoice() {
        Card graveyardSorin = new SorinVampireLord();
        Card librarySorin = new SorinVampireLord();
        harness.setGraveyard(player1, List.of(graveyardSorin));
        harness.setLibrary(player1, List.of(librarySorin));
        setupAndCast();

        resolveMayPrompt(true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardSorin);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
