package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavenousBloodseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card gives this creature +2/-2 until end of turn")
    void discardBoostsPowerAndReducesToughness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bloodseeker = harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        int basePower = gqs.getEffectivePower(gd, bloodseeker);
        int baseToughness = gqs.getEffectiveToughness(gd, bloodseeker);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bloodseeker)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bloodseeker)).isEqualTo(baseToughness - 2);
    }

    @Test
    @DisplayName("The +2/-2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bloodseeker = harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        int basePower = gqs.getEffectivePower(gd, bloodseeker);
        int baseToughness = gqs.getEffectiveToughness(gd, bloodseeker);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bloodseeker)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bloodseeker)).isEqualTo(baseToughness - 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodseeker)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bloodseeker)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
