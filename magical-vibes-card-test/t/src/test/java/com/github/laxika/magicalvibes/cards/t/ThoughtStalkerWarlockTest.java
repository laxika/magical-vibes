package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtStalkerWarlock.class, Forest.class})
class ThoughtStalkerWarlockTest extends BaseCardTest {

    @Test
    @DisplayName("If the target opponent lost life, you choose a nonland card to discard")
    void targetLostLifeAllowsChoosingNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new ThoughtStalkerWarlock(), new Forest())));
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        castThoughtStalkerWarlock();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Thought-Stalker Warlock");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Forest");
    }

    @Test
    @DisplayName("If the target opponent did not lose life, they choose a card to discard")
    void targetDidNotLoseLifeDiscardsOneCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new ThoughtStalkerWarlock(), new Forest())));
        castThoughtStalkerWarlock();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);

        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Thought-Stalker Warlock");
    }

    @Test
    void lifeLostAfterTriggeringIsCheckedAtResolution() {
        harness.setHand(player2, List.of(new ThoughtStalkerWarlock(), new Forest()));
        putThoughtStalkerWarlockTriggerOnStack();
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player2, "Thought-Stalker Warlock");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void gainingLifeDoesNotUndoEarlierLifeLoss() {
        harness.setHand(player2, List.of(new ThoughtStalkerWarlock(), new Forest()));
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLife(player2, 21);

        castThoughtStalkerWarlock();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player2, "Thought-Stalker Warlock");
    }

    @Test
    void controllerLifeLossDoesNotUpgradeOpponentsDiscard() {
        harness.setHand(player2, List.of(new ThoughtStalkerWarlock(), new Forest()));
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        castThoughtStalkerWarlock();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void landOnlyHandIsRevealedWithoutDiscardAfterLifeLoss() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castThoughtStalkerWarlock();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void emptyHandCompletesEitherBranch(int lifeLost) {
        harness.setHand(player2, List.of());
        gd.lifeLostThisTurn.put(player2.getId(), lifeLost);

        castThoughtStalkerWarlock();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castThoughtStalkerWarlock() {
        putThoughtStalkerWarlockTriggerOnStack();
        harness.passBothPriorities();
    }

    private void putThoughtStalkerWarlockTriggerOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ThoughtStalkerWarlock(), "{2}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
    }
}
