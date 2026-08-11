package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimidShieldbearerTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives +1/+1 to all creatures you control")
    void activationBoostsOwnCreatures() {
        Permanent shieldbearer = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shieldbearer.getPowerModifier()).isEqualTo(1);
        assertThat(shieldbearer.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activation boost expires at end of turn")
    void activationBoostExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TimidShieldbearer());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
