package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aquamoeba.class, GrizzlyBears.class})
class AquamoebaTest extends BaseCardTest {

    @Test
    void discardingSwitchesPowerAndToughnessUntilEndOfTurn() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void switchedPowerAndToughnessWearOffAtEndOfTurn() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        activateAndDiscard();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(3);
    }

    private Permanent addReadyAquamoeba() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent aquamoeba = harness.addToBattlefieldAndReturn(player1, new Aquamoeba());
        aquamoeba.setSummoningSick(false);
        return aquamoeba;
    }

    private void activateAndDiscard() {
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
