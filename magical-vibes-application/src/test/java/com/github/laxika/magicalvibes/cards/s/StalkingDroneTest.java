package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StalkingDrone.class)
class StalkingDroneTest extends BaseCardTest {

    @Test
    void colorlessManaBoostsStalkingDrone() {
        Permanent drone = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent drone = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(2);
    }

    private Permanent addReadyDrone() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new StalkingDrone());
        drone.setSummoningSick(false);
        return drone;
    }
}
