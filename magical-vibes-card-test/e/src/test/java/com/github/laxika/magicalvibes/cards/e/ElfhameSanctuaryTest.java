package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElfhameSanctuary.class, ElfhamePalace.class, Forest.class, Plains.class})
class ElfhameSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Searching for a basic land puts it into hand and skips this turn's draw")
    void searchesForBasicLandAndSkipsDraw() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new Plains(), new ElfhamePalace()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        int handBeforeSearch = gd.playerHands.get(player1.getId()).size();
        int deckBeforeSearch = gd.playerDecks.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeSearch + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeSearch - 1);
        harness.assertInHand(player1, "Plains");
        assertThat(gameLogContains("reveals Plains")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();

        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeDraw);
    }

    @Test
    @DisplayName("Failing to find still skips this turn's draw")
    void failingToFindStillSkipsDraw() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new ElfhamePalace(), new Forest()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeDraw);
    }

    @Test
    @DisplayName("A nonbasic land cannot be found, but accepting the search still skips this turn's draw")
    void nonbasicLandIsNotFoundAndStillSkipsDraw() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new ElfhamePalace()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeDraw);
    }

    @Test
    @DisplayName("An empty library still allows the accepted search to skip this turn's draw")
    void emptyLibraryStillSkipsDraw() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of());

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeDraw);
    }

    @Test
    @DisplayName("Declining the search leaves the draw step unchanged")
    void decliningSearchDoesNotSkipDraw() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new Plains(), new ElfhamePalace()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.skipDrawStepThisTurn).doesNotContainKey(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeDraw - 1);
    }

    @Test
    @DisplayName("Multiple Sanctuaries skip only the current draw step")
    void multipleSanctuariesDoNotSkipAnotherDrawStep() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new ElfhamePalace()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeDraw);
        assertThat(gd.skipDrawStepThisTurn).doesNotContainKey(player1.getId());
        assertThat(gd.skipNextDrawStepCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Skipping the draw step bypasses its priority window")
    void skippedDrawStepHasNoPriorityWindow() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new ElfhamePalace()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
                    harness.handleMayAbilityChosen(player1, true);
                    harness.passBothPriorities();
                    assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
                }));
    }

    @Test
    @DisplayName("Sanctuary does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player2, List.of(new Plains(), new Forest()));
        gd.turnNumber = 2;
        int handBeforeDraw = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBeforeDraw + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting a search does not skip a later turn's draw")
    void acceptingSearchDoesNotSkipLaterTurnsDraw() {
        harness.addToBattlefield(player1, new ElfhameSanctuary());
        harness.setLibrary(player1, List.of(new ElfhamePalace(), new ElfhamePalace()));

        advanceToSanctuaryUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        gd.turnNumber += 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        int handBeforeDraw = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDraw + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void advanceToSanctuaryUpkeep() {
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
    }
}
