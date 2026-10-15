package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({RavenousBloodseeker.class, Mountain.class})
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
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
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
        harness.setHand(player1, List.of(new Mountain()));

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

    @Test
    @DisplayName("Discard is paid before resolution and the boost affects only the source")
    void discardIsAnUpfrontCostAndBoostIsSelfOnly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        int basePower = gqs.getEffectivePower(gd, source);
        int baseToughness = gqs.getEffectiveToughness(gd, source);
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(baseToughness);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(baseToughness - 2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("A second activation stacks the toughness reduction and kills the creature")
    void repeatedActivationCausesDeathFromNonpositiveToughness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ravenous Bloodseeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ravenous Bloodseeker");
        harness.assertInGraveyard(player1, "Ravenous Bloodseeker");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain")).hasSize(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick creature can activate on the opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RavenousBloodseeker());
        source.tap();
        source.setSummoningSick(true);
        int basePower = gqs.getEffectivePower(gd, source);
        int baseToughness = gqs.getEffectiveToughness(gd, source);
        harness.setHand(player1, List.of(new Mountain()));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(baseToughness - 2);
        assertThat(source.isTapped()).isTrue();
    }
}
