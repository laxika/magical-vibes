package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TowashiSongshaper.class, FountainOfYouth.class})
class TowashiSongshaperTest extends BaseCardTest {

    @Test
    void anotherArtifactEnteringUnderYourControlBoostsTowashiSongshaper() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.passBothPriorities();

        assertThat(songshaper.getPowerModifier()).isEqualTo(1);
        assertThat(songshaper.getToughnessModifier()).isZero();
    }

    @Test
    void opponentArtifactDoesNotTriggerTowashiSongshaper() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.passBothPriorities();

        assertThat(songshaper.getPowerModifier()).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.passBothPriorities();
        assertThat(songshaper.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(songshaper.getPowerModifier()).isZero();
    }
}
