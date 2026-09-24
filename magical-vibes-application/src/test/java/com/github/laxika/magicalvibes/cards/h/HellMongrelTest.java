package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellMongrel.class, GrizzlyBears.class})
class HellMongrelTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card gives this creature +1/+1 until end of turn")
    void discardBoostsPlusOneOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        int basePower = gqs.getEffectivePower(gd, mongrel);
        int baseToughness = gqs.getEffectiveToughness(gd, mongrel);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        int basePower = gqs.getEffectivePower(gd, mongrel);
        int baseToughness = gqs.getEffectiveToughness(gd, mongrel);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
