package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalkingDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Walking Dream cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        dream.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Walking Dream untaps when each opponent controls fewer than two creatures")
    void untapsBelowOpponentCreatureThreshold() {
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        addCreatureReady(player2, new GrizzlyBears());
        dream.tap();

        advanceToNextTurn(player2);

        assertThat(dream.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Walking Dream stays tapped when an opponent controls two creatures")
    void staysTappedAtOpponentCreatureThreshold() {
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        dream.tap();

        advanceToNextTurn(player2);

        assertThat(dream.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
