package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RagingKavu.class)
class RagingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Flash lets Raging Kavu be cast during an opponent's turn and haste lets it attack immediately")
    void flashesInAndAttacksImmediately() {
        harness.addToBattlefield(player2, new RagingKavu());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castFromHand(player1, new RagingKavu(), "{1}{R}{G}");
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Raging Kavu");

        harness.forceActivePlayer(player1);
        declareAttackers(player1, List.of(0));

        assertThat(kavu.isAttacking()).isTrue();
    }
}
