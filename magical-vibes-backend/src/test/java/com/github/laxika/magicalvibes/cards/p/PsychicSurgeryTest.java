package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicSurgeryTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers may ability when opponent shuffles their library")
    void triggersOnOpponentShuffle() {
        harness.addToBattlefield(player1, new PsychicSurgery());

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        assertThat(gd.pendingMayAbilities).hasSize(1);
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        assertThat(pending.controllerId()).isEqualTo(player1.getId());
        assertThat(pending.sourceCard().getName()).isEqualTo("Psychic Surgery");
    }

    @Test
    @DisplayName("Does not trigger when controller shuffles their own library")
    void doesNotTriggerOnOwnShuffle() {
        harness.addToBattlefield(player1, new PsychicSurgery());

        LibraryShuffleHelper.shuffleLibrary(gd, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Declining the may ability does nothing")
    void decliningMayAbilityDoesNothing() {
        harness.addToBattlefield(player1, new PsychicSurgery());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int exileSizeBefore = gd.getPlayerExiledCards(player1.getId()).size();

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        // Manually begin the may ability interaction
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        gd.interaction.beginMayAbilityChoice(pending.controllerId(), pending.description());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(exileSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting and exiling a card removes it from opponent's library")
    void acceptAndExileCard() {
        harness.addToBattlefield(player1, new PsychicSurgery());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Trigger shuffle
        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        // Set up known cards on top of player2's deck after shuffle
        Card topCard = new Island();
        Card secondCard = new Forest();
        Card thirdCard = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(topCard, secondCard, thirdCard));

        // Begin and accept may ability
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        gd.interaction.beginMayAbilityChoice(pending.controllerId(), pending.description());
        harness.handleMayAbilityChosen(player1, true);

        // Stack entry created, pass priorities to resolve
        harness.passBothPriorities();

        // Should be in LIBRARY_SEARCH state
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Choose to exile the first card (index 0 = Island)
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Island should be exiled in player2's exile zone (card owner's zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));

        // Forest should be on top of player2's library, GrizzlyBears after it
        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter.getFirst().getId()).isEqualTo(secondCard.getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(thirdCard.getId());
    }

    @Test
    @DisplayName("Choosing not to exile puts both cards back on top")
    void chooseNotToExile() {
        harness.addToBattlefield(player1, new PsychicSurgery());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Trigger shuffle
        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        // Set up known cards on top of player2's deck after shuffle
        Card topCard = new Island();
        Card secondCard = new Forest();
        Card thirdCard = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(topCard, secondCard, thirdCard));

        int exileSizeBefore = gd.getPlayerExiledCards(player1.getId()).size();

        // Begin and accept may ability
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        gd.interaction.beginMayAbilityChoice(pending.controllerId(), pending.description());
        harness.handleMayAbilityChosen(player1, true);

        // Pass priorities to resolve
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Choose not to exile (-1)
        gs.handleLibraryCardChosen(gd, player1, -1);

        // Nothing should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(exileSizeBefore);

        // Should be in LIBRARY_REORDER state to reorder the 2 cards on top
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);

        // Reorder: put them back in order [0, 1] (Island on top, Forest second)
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1));

        // Both cards should be on top of player2's library
        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter.get(0).getId()).isEqualTo(topCard.getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(secondCard.getId());
        assertThat(deckAfter.get(2).getId()).isEqualTo(thirdCard.getId());
    }

    @Test
    @DisplayName("Empty opponent library resolves without error")
    void emptyOpponentLibrary() {
        harness.addToBattlefield(player1, new PsychicSurgery());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Empty the opponent's library
        gd.playerDecks.get(player2.getId()).clear();

        // Trigger shuffle
        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        // Begin and accept may ability
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        gd.interaction.beginMayAbilityChoice(pending.controllerId(), pending.description());
        harness.handleMayAbilityChosen(player1, true);

        // Pass priorities to resolve — should handle empty library gracefully
        harness.passBothPriorities();

        // No library search should be initiated since library is empty
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("Only looks at 1 card if opponent library has only 1 card")
    void singleCardLibrary() {
        harness.addToBattlefield(player1, new PsychicSurgery());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Trigger shuffle
        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        // Set player2's deck to single card
        Card onlyCard = new Island();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.add(onlyCard);

        // Begin and accept may ability
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        gd.interaction.beginMayAbilityChoice(pending.controllerId(), pending.description());
        harness.handleMayAbilityChosen(player1, true);

        // Pass priorities to resolve
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Exile the only card
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Card should be exiled in player2's exile zone (card owner's zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(onlyCard.getId()));

        // Library should be empty
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
