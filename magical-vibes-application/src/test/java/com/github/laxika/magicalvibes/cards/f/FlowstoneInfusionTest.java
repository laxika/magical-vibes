package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneInfusion.class, AirElemental.class, FountainOfYouth.class})
class FlowstoneInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/-2 until end of turn")
    void givesPlusTwoMinusTwo() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castFlowstoneInfusion(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castFlowstoneInfusion(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FlowstoneInfusion()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFlowstoneInfusion(Permanent target) {
        harness.setHand(player1, List.of(new FlowstoneInfusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
