package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RakdosTrumpeterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Rakdos Trumpeter +2/+0 until end of turn")
    void activationBoostsSelf() {
        Permanent trumpeter = addReadyTrumpeter();
        int basePower = gqs.getEffectivePower(gd, trumpeter);
        int baseToughness = gqs.getEffectiveToughness(gd, trumpeter);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trumpeter)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent trumpeter = addReadyTrumpeter();
        int basePower = gqs.getEffectivePower(gd, trumpeter);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower);
    }

    private Permanent addReadyTrumpeter() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new RakdosTrumpeter());
        trumpeter.setSummoningSick(false);
        return trumpeter;
    }
}
