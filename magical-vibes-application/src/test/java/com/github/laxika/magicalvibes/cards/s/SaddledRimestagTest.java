package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaddledRimestag.class, GrizzlyBears.class})
class SaddledRimestagTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 after another creature enters under its controller's control")
    void getsBoostAfterAnotherCreatureEntersUnderItsControllersControl() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's creature entering does not grant the boost")
    void opponentCreatureEnteringDoesNotGrantBoost() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when the turn ends")
    void boostEndsWhenTurnEnds() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);
    }
}
