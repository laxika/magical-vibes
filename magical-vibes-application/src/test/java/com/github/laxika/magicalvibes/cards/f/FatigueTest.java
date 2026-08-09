package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FatigueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fatigue queues a draw-step skip for the target player")
    void queuesDrawStepSkipForTargetPlayer() {
        harness.setHand(player1, List.of(new Fatigue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
        assertThat(gd.skipNextDrawStepCount).doesNotContainKey(player2.getId());
    }
}
