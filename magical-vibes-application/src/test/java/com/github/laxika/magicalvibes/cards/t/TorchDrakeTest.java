package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TorchDrake.class)
class TorchDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R}: Torch Drake gets +1/+0 until end of turn")
    void boostsPower() {
        Permanent drake = addReadyTorchDrake();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Torch Drake's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent drake = addReadyTorchDrake();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    private Permanent addReadyTorchDrake() {
        Permanent drake = new Permanent(new TorchDrake());
        drake.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(drake);
        return drake;
    }
}
