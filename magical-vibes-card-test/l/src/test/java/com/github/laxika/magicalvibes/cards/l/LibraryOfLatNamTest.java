package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LibraryOfLatNam.class, GrizzlyBears.class})
class LibraryOfLatNamTest extends BaseCardTest {

    private static final String FIRST_MODE = "You draw three cards at the beginning of the next turn's upkeep";
    private static final String SECOND_MODE =
            "You search your library for a card and put it into your hand";

    @Test
    @DisplayName("Casting prompts the opponent to choose a mode")
    void castingPromptsOpponentChoice() {
        setupAndCast();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accept schedules the controller drawing three cards at the next upkeep")
    void acceptSchedulesDelayedDraw() {
        setupAndCast();
        setupLibrary();

        GameData gd = harness.getGameData();

        harness.handleListChoice(player2, FIRST_MODE);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
        harness.passBothPriorities();

        List<TemporaryGlobalTriggeredAbility> scheduled = gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();

    }

    @Test
    @DisplayName("Decline lets the controller search their library for a card to hand")
    void declineBeginsLibrarySearch() {
        setupAndCast();
        setupLibrary();

        GameData gd = harness.getGameData();

        harness.handleListChoice(player2, SECOND_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());

        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
    }

    @Test
    @DisplayName("Declining with an empty library finishes without a search choice")
    void declineWithEmptyLibraryFinishesWithoutChoice() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.handleListChoice(player2, SECOND_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
    }

    @Test
    @DisplayName("The search can find a noncreature card and finishes during spell resolution")
    void searchFindsNoncreatureCardDuringResolution() {
        LibraryOfLatNam searchedCard = new LibraryOfLatNam();
        harness.setLibrary(player1, List.of(searchedCard));
        setupAndCast();

        harness.handleListChoice(player2, SECOND_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.stack).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(searchedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new LibraryOfLatNam(), "{4}{U}");
    }

    private void setupLibrary() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            deck.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, deck);
    }
}
