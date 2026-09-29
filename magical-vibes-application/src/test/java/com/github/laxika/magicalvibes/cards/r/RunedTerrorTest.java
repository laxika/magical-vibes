package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RunedTerror.class)
class RunedTerrorTest extends BaseCardTest {

    @Test
    void playersTakeBeginningAndMainPhasesSequentially() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        terror.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);

        harness.getGameService().advanceStep(gd);
        harness.getGameService().advanceStep(gd);
        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }
}
