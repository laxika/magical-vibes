package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingPhoenix.class, GrizzlyBears.class})
class ScreechingPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives creatures you control +1/+0")
    void boostsOwnCreatures() {
        Permanent phoenix = addPhoenix(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateAbility(player1);

        assertThat(phoenix.getPowerModifier()).isEqualTo(1);
        assertThat(phoenix.getToughnessModifier()).isZero();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(opponentBears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Activated ability bonus wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addPhoenix(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        activateAbility(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    private Permanent addPhoenix(Player player) {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player, new ScreechingPhoenix());
        phoenix.setSummoningSick(false);
        return phoenix;
    }

    private void activateAbility(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }
}
