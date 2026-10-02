package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Aquamoeba.class)
class AquamoebaTest extends BaseCardTest {

    @Test
    void discardingSwitchesPowerAndToughnessUntilEndOfTurn() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, List.of(new Aquamoeba()));

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Aquamoeba");
    }

    @Test
    void switchedPowerAndToughnessWearOffAtEndOfTurn() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, List.of(new Aquamoeba()));

        activateAndDiscard();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(3);
    }

    @Test
    void canActivateWhileSummoningSickBecauseAbilityDoesNotTap() {
        Permanent aquamoeba = addAquamoeba();
        harness.setHand(player1, List.of(new Aquamoeba()));

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(1);
    }

    @Test
    void discardIsPaidBeforeTheAbilityResolves() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, List.of(new Aquamoeba()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Aquamoeba");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(1);
    }

    @Test
    void twoActivationsSwitchPowerAndToughnessBack() {
        Permanent aquamoeba = addReadyAquamoeba();
        harness.setHand(player1, List.of(new Aquamoeba(), new Aquamoeba()));

        activateAndDiscard();
        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canActivateWhileTappedOnOpponentsTurn() {
        Permanent aquamoeba = addReadyAquamoeba();
        aquamoeba.tap();
        harness.setHand(player1, List.of(new Aquamoeba()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aquamoeba)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aquamoeba)).isEqualTo(1);
        assertThat(aquamoeba.isTapped()).isTrue();
    }
    private Permanent addReadyAquamoeba() {
        prepareAquamoebaActivation();
        return addCreatureReady(player1, new Aquamoeba());
    }

    private Permanent addAquamoeba() {
        prepareAquamoebaActivation();
        return harness.addToBattlefieldAndReturn(player1, new Aquamoeba());
    }

    private void prepareAquamoebaActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateAndDiscard() {
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
