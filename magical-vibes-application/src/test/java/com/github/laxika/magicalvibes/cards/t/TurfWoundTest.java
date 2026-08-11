package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurfWoundTest extends BaseCardTest {

    private void castAtPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TurfWound()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private List<Integer> player2Playable() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.ensurePriority(player2);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId());
    }

    @Test
    @DisplayName("Target can't play lands this turn but can still cast creatures")
    void blocksLandsButNotCreatures() {
        castAtPlayer2();

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0);
        assertThat(playable).contains(1);
    }

    @Test
    @DisplayName("Land restriction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        castAtPlayer2();
        assertThat(player2Playable()).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(player2Playable()).contains(0);
    }

    @Test
    @DisplayName("Controller draws a card immediately")
    void controllerDrawsCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castAtPlayer2();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }
}
