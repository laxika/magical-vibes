package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThalakosSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Leaving the battlefield draws a card")
    void leavingDrawsACard() {
        harness.addToBattlefield(player1, new ThalakosSeer());

        Permanent seer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ThalakosSeer)
                .findFirst().orElseThrow();

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, seer));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger resolves

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
