package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RidgetopRaptor.class)
class RidgetopRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Has double strike")
    void hasDoubleStrike() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RidgetopRaptor());

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent raptor = addCreatureReady(player1, new RidgetopRaptor());
        raptor.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
