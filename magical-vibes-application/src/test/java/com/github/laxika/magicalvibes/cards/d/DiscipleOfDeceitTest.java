package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscipleOfDeceitTest extends BaseCardTest {

    @Test
    @DisplayName("Inspired can discard a nonland card and search for the same mana value")
    void inspiredDiscardsNonlandAndSearchesForSameManaValue() {
        Card discarded = new GrizzlyBears();
        Card landInHand = new Island();
        Card searchTarget = new GrizzlyBears();
        Card differentManaValue = new Island();
        addTappedDisciple();
        harness.setHand(player1, new ArrayList<>(List.of(discarded, landInHand)));
        harness.setLibrary(player1, List.of(searchTarget, differentManaValue));

        resolveUntapTrigger();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch librarySearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(librarySearch.params().cards()).containsExactly(searchTarget);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(landInHand, searchTarget);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Declining Inspired does not discard or search")
    void decliningInspiredDoesNothing() {
        Card cardInHand = new GrizzlyBears();
        addTappedDisciple();
        harness.setHand(player1, new ArrayList<>(List.of(cardInHand)));
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
        harness.setHand(player1, new ArrayList<>(List.of(landInHand)));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveUntapTrigger();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(landInHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
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
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
