package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.t.TeferiTimebender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NiambiFaithfulHealer.class, TeferiTimebender.class})
class NiambiFaithfulHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Niambi triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Teferi, Timebender in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card teferi = new TeferiTimebender();
        harness.setGraveyard(player1, List.of(teferi));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Teferi, Timebender");
        harness.assertNotInGraveyard(player1, "Teferi, Timebender");
    }

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card teferi = new TeferiTimebender();
        harness.setLibrary(player1, List.of(teferi));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        // Library search prompt should appear
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Teferi, Timebender");
    }

    @Test
    @DisplayName("Accepting may when Teferi is not in library or graveyard does nothing")
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
        Card teferi = new TeferiTimebender();
        harness.setGraveyard(player1, List.of(teferi));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        // Teferi stays in graveyard
        harness.assertInGraveyard(player1, "Teferi, Timebender");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Niambi enters the battlefield after resolving")
    void niambiEntersBattlefield() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Niambi, Faithful Healer");
    }


    @Test
    @DisplayName("Library search puts the chosen Teferi into hand")
    void choosingLibraryCardMovesItToHand() {
        Card teferi = new TeferiTimebender();
        harness.setLibrary(player1, List.of(teferi));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(teferi);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(teferi);
    }

    @Test
    @DisplayName("Library search may fail to find even when Teferi is present")
    void librarySearchMayFailToFind() {
        Card teferi = new TeferiTimebender();
        harness.setLibrary(player1, List.of(teferi));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(teferi);
    }

    @Test
    @DisplayName("A graveyard match must not prevent choosing a library match")
    void graveyardMatchDoesNotForceImmediateRetrieval() {
        Card graveyardTeferi = new TeferiTimebender();
        Card libraryTeferi = new TeferiTimebender();
        harness.setGraveyard(player1, List.of(graveyardTeferi));
        harness.setLibrary(player1, List.of(libraryTeferi));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardTeferi);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new NiambiFaithfulHealer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }

}
