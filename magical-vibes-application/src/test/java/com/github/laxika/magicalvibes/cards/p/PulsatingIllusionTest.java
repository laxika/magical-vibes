package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PulsatingIllusion.class, AvenFlock.class})
class PulsatingIllusionTest extends BaseCardTest {

    @Test
    void discardingACardGivesItPlusFourPlusFourUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent illusion = harness.addToBattlefieldAndReturn(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, illusion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, illusion)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Aven Flock");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, illusion)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, illusion)).isEqualTo(1);
    }

    @Test
    void canActivateAgainOnTheFollowingTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent illusion = harness.addToBattlefieldAndReturn(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock(), new AvenFlock()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, illusion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, illusion)).isEqualTo(5);
    }

    @Test
    void canActivateOnlyOnceEachTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent illusion = harness.addToBattlefieldAndReturn(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock(), new AvenFlock()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, illusion)).isEqualTo(4);
    }

    @Test
    void cannotActivateWithoutACardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
