package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KragmaButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming untapped gives Kragma Butcher +2/+0 until end of turn")
    void becomingUntappedBoostsItUntilEndOfTurn() {
        Permanent butcher = addTappedButcher(player1);
        int basePower = gqs.getEffectivePower(gd, butcher);
        int baseToughness = gqs.getEffectiveToughness(gd, butcher);

        runUntapStep(player1);
        resolveStack();

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, butcher)).isEqualTo(baseToughness);

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(basePower);
    }

    private Permanent addTappedButcher(Player player) {
        Permanent butcher = harness.addToBattlefieldAndReturn(player, new KragmaButcher());
        butcher.setSummoningSick(false);
        butcher.tap();
        return butcher;
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
