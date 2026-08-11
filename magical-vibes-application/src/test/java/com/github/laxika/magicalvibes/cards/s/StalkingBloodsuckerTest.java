package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StalkingBloodsuckerTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("{1}{B}, Discard a card gives this creature +2/+2 until end of turn")
    void discardBoostsPlusTwoPlusTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new StalkingBloodsucker());
        int basePower = gqs.getEffectivePower(gd, bloodsucker);
        int baseToughness = gqs.getEffectiveToughness(gd, bloodsucker);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new StalkingBloodsucker());
        int basePower = gqs.getEffectivePower(gd, bloodsucker);
        int baseToughness = gqs.getEffectiveToughness(gd, bloodsucker);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(baseToughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new StalkingBloodsucker());
        harness.setHand(player1, new ArrayList<>());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
