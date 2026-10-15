package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfDeceit.class, GrizzlyBears.class, Island.class})
class DiscipleOfDeceitTest extends BaseCardTest {

    @Test
    @DisplayName("Inspired can discard a nonland card and search for the same mana value")
    void inspiredDiscardsNonlandAndSearchesForSameManaValue() {
        Card discarded = new GrizzlyBears();
        Card landInHand = new Island();
        Card searchTarget = new GrizzlyBears();
        Card differentManaValue = new Island();
        addTappedDisciple();
        harness.setHand(player1, List.of(discarded, landInHand));
        harness.setLibrary(player1, List.of(searchTarget, differentManaValue));

        resolveUntapTrigger();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.stack).isEmpty();

        PendingInteraction.LibrarySearch librarySearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(librarySearch.params().cards()).containsExactly(searchTarget);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(landInHand, searchTarget);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Declining Inspired does not discard or search")
    void decliningInspiredDoesNothing() {
        Card cardInHand = new GrizzlyBears();
        addTappedDisciple();
        harness.setHand(player1, List.of(cardInHand));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveUntapTrigger();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(cardInHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Inspired cannot discard a land")
    void inspiredCannotDiscardLand() {
        Card landInHand = new Island();
        addTappedDisciple();
        harness.setHand(player1, List.of(landInHand));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveUntapTrigger();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(landInHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discarding still happens when the library has no matching card")
    void noMatchingCardStillDiscardsAndShuffles() {
        Card discarded = new GrizzlyBears();
        Card land = new Island();
        addTappedDisciple();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(land));

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            resolveUntapTrigger();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The controller may fail to find even when a matching card exists")
    void mayFailToFindMatchingCard() {
        Card discarded = new GrizzlyBears();
        Card matching = new GrizzlyBears();
        addTappedDisciple();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(matching));

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            resolveUntapTrigger();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matching);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An already untapped Disciple does not trigger Inspired")
    void alreadyUntappedDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiscipleOfDeceit());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveUntapTrigger();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addTappedDisciple() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfDeceit());
        disciple.setSummoningSick(false);
        disciple.tap();
    }

    private void resolveUntapTrigger() {
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
