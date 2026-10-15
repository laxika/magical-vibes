package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fatigue.class})
class FatigueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fatigue queues a draw-step skip for the target player")
    void queuesDrawStepSkipForTargetPlayer() {
        harness.setHand(player1, List.of(new Fatigue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.skipNextDrawStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The target player skips their next draw step")
    void targetPlayerSkipsNextDrawStep() {
        harness.setHand(player1, List.of(new Fatigue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
        assertThat(gd.skipNextDrawStepCount).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("A skipped draw step offers no priority even with a draw-step stop configured")
    void skippedDrawStepDoesNotOfferPriority() {
        harness.setHand(player1, List.of(new Fatigue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.skipNextDrawStepCount).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Two Fatigues skip two successive draw steps, then drawing resumes")
    void multipleFatiguesSkipSuccessiveDrawSteps() {
        harness.setHand(player1, List.of(new Fatigue(), new Fatigue()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Fatigue(), new Fatigue()));
        harness.forceActivePlayer(player2);

        for (int turn = 2; turn <= 4; turn++) {
            gd.turnNumber = turn;
            harness.forceStep(TurnStep.UPKEEP);
            harness.passUntil(TurnStep.PRECOMBAT_MAIN);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(turn == 4 ? 1 : 0);
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(turn == 4 ? 1 : 2);
            assertThat(gd.skipNextDrawStepCount.getOrDefault(player2.getId(), 0))
                    .isEqualTo(Math.max(3 - turn, 0));
        }
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Fatigue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.skipNextDrawStepCount).containsEntry(player1.getId(), 1);
        assertThat(gd.skipNextDrawStepCount).doesNotContainKey(player2.getId());
    }
}
