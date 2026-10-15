package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.l.LeoninArbiter;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistantMemories.class, Plains.class, Swamp.class, MyrSire.class,
        LeoninArbiter.class, PlatinumAngel.class, PsychogenicProbe.class})
class DistantMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Distant Memories puts it on the stack")
    void castingPutsOnStack() {
        setupAndCast();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(DistantMemories.class);
    }

    @Test
    @DisplayName("Resolving Distant Memories presents library for search")
    void resolvingPresentsLibrarySearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(4);
    }

    @Test
    @DisplayName("When opponent accepts, exiled card goes to controller's hand")
    void opponentAcceptsCardGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();

        // Player 1 chooses a card from library
        harness.handleCardChosen(player1, 0);

        // Should now be awaiting opponent's may ability choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Opponent allows the controller to put the card into hand.
        harness.handleMayAbilityChosen(player2, true);

        // Card should be in player1's hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, chosenName);

        // Card should no longer be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("When opponent declines, controller draws three cards")
    void opponentDeclinesControllerDrawsThree() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();

        // Player 1 chooses a card from library
        harness.handleCardChosen(player1, 0);

        // Should now be awaiting opponent's may ability choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Opponent declines, so the controller draws three cards.
        harness.handleMayAbilityChosen(player2, false);

        // Player 1 should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);

        // Deck should have 3 fewer cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);

        // The exiled card should still be in exile (not returned)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("Chosen card is exiled and library is shuffled before opponent choice")
    void chosenCardIsExiledAndLibraryShuffled() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        // Card should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));

        // Library should have lost one card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);

        // Log should mention exile and shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("exiles a card"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("shuffled"));
    }

    @Test
    @DisplayName("Drawing from an empty library after the search makes the controller lose")
    void emptyLibraryDrawsThree() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should NOT be in library search mode (library was empty)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Distant Memories goes to graveyard after full resolution")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Distant Memories");
    }

    @Test
    @DisplayName("Unrestricted search sets canFailToFind to false")
    void cannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("A mandatory search rejects failing to find and still allows a valid choice")
    void failingToFindIsRejected() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("An opponent can return the last library card without making the controller draw")
    void acceptingLastCardDoesNotDraw() {
        Plains chosenCard = new Plains();
        setupAndCast();
        harness.setLibrary(player1, List.of(chosenCard));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.findExiledCard(chosenCard.getId()).faceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Distant Memories");
    }

    @Test
    @DisplayName("Declining with fewer than three cards remaining draws what is available and loses")
    void decliningWithShortLibraryLoses() {
        Plains chosenCard = new Plains();
        Swamp remainingCard = new Swamp();
        setupAndCast();
        harness.setLibrary(player1, List.of(chosenCard, remainingCard));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @CardUsed({DistantMemories.class, Plains.class, Swamp.class, MyrSire.class, LeoninArbiter.class})
    @DisplayName("Preventing the search does not prevent the three-card draw")
    void preventedSearchStillDrawsThree() {
        harness.addToBattlefield(player2, new LeoninArbiter());
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Distant Memories");
    }

    @Test
    @CardUsed({DistantMemories.class, PlatinumAngel.class, PsychogenicProbe.class})
    @DisplayName("An empty library is still shuffled and triggers Psychogenic Probe")
    void emptyLibraryStillTriggersShuffle() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof PsychogenicProbe);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @CardUsed({DistantMemories.class, Plains.class})
    @DisplayName("The second player's opponent returns the searched card to the second player's hand")
    void secondPlayerCanReceiveSearchedCard() {
        Plains chosenCard = new Plains();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new DistantMemories(), "{2}{U}{U}");
        harness.setLibrary(player2, List.of(chosenCard));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosenCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player2, "Distant Memories");
    }

    @Test
    @CardUsed({DistantMemories.class, Plains.class, LeoninArbiter.class})
    @DisplayName("Paying Leonin Arbiter's tax before resolution allows the search and return")
    void payingSearchTaxAllowsSearch() {
        Plains chosenCard = new Plains();
        harness.addToBattlefield(player2, new LeoninArbiter());
        setupAndCast();
        harness.setLibrary(player1, List.of(chosenCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.paySearchTax(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Distant Memories");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new DistantMemories(), "{2}{U}{U}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new MyrSire(), new MyrSire()));
    }
}
