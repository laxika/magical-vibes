package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TirelessTribe.class})
class TirelessTribeTest extends BaseCardTest {

    @Test
    void discardingACardGivesTirelessTribePlusZeroPlusFourUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        harness.setHand(player1, List.of(new TirelessTribe()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tribe)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Tireless Tribe");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tribe)).isEqualTo(1);
    }

    @Test
    void canActivateMultipleTimesInOneTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        harness.setHand(player1, List.of(new TirelessTribe(), new TirelessTribe()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tribe)).isEqualTo(9);
        harness.assertNotInHand(player1, "Tireless Tribe");
    }

    @Test
    void discardIsPaidBeforeTheBoostResolvesAndOnlyTheSourceIsBoosted() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        Permanent otherTribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        Permanent opposingTribe = harness.addToBattlefieldAndReturn(player2, new TirelessTribe());
        harness.setHand(player1, List.of(new TirelessTribe()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Tireless Tribe");
        harness.assertInGraveyard(player1, "Tireless Tribe");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveToughness(gd, tribe)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tribe)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherTribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingTribe)).isEqualTo(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        tribe.tap();
        tribe.setSummoningSick(true);
        harness.setHand(player1, List.of(new TirelessTribe()));
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tribe)).isEqualTo(5);
        assertThat(tribe.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Tireless Tribe");
    }

    @Test
    void cannotActivateWithoutACardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
