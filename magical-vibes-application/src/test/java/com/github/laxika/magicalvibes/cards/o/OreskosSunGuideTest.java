package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OreskosSunGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Oreskos Sun Guide makes its controller gain 2 life")
    void untappingGainsLife() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        guide.tap();
        harness.setLife(player1, 10);

        runUntapStep(player1);
        resolveStack();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
