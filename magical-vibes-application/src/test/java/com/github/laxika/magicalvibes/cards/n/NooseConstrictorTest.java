package com.github.laxika.magicalvibes.cards.n;

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

class NooseConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card gives this creature +1/+1 until end of turn")
    void discardBoostsPlusOneOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        int basePower = gqs.getEffectivePower(gd, constrictor);
        int baseToughness = gqs.getEffectiveToughness(gd, constrictor);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0); // pay the discard cost
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        int basePower = gqs.getEffectivePower(gd, constrictor);
        int baseToughness = gqs.getEffectiveToughness(gd, constrictor);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
