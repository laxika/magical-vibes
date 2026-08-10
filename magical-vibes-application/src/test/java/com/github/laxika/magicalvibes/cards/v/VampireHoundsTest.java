package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VampireHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card gives Vampire Hounds +2/+2 until end of turn")
    void discardingCreatureBoostsVampireHounds() {
        Permanent hounds = harness.addToBattlefieldAndReturn(player1, new VampireHounds());
        int basePower = gqs.getEffectivePower(gd, hounds);
        int baseToughness = gqs.getEffectiveToughness(gd, hounds);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, hounds)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, hounds)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The +2/+2 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hounds = harness.addToBattlefieldAndReturn(player1, new VampireHounds());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(hounds.getPowerModifier()).isEqualTo(2);
        assertThat(hounds.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hounds.getPowerModifier()).isZero();
        assertThat(hounds.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefieldAndReturn(player1, new VampireHounds());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefieldAndReturn(player1, new VampireHounds());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
