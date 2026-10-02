package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Aeromoeba.class)
class AeromoebaTest extends BaseCardTest {

    @Test
    void discardingSwitchesPowerAndToughnessUntilEndOfTurn() {
        Permanent aeromoeba = addReadyAeromoeba();
        harness.setHand(player1, List.of(new Aeromoeba()));

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Aeromoeba");
    }

    @Test
    void switchedPowerAndToughnessWearOffAtEndOfTurn() {
        Permanent aeromoeba = addReadyAeromoeba();
        harness.setHand(player1, List.of(new Aeromoeba()));

        activateAndDiscard();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(4);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent aeromoeba = addReadyAeromoeba();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(4);
    }

    @Test
    void canActivateWhileSummoningSickBecauseAbilityDoesNotTap() {
        prepareAeromoebaActivation();
        Permanent aeromoeba = harness.addToBattlefieldAndReturn(player1, new Aeromoeba());
        harness.setHand(player1, List.of(new Aeromoeba()));

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(2);
    }

    @Test
    void discardIsPaidBeforeTheSwitchResolves() {
        Permanent aeromoeba = addReadyAeromoeba();
        harness.setHand(player1, List.of(new Aeromoeba()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Aeromoeba");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(2);
    }

    @Test
    void successiveActivationsSwitchBackAndThenSwitchAgain() {
        Permanent aeromoeba = addReadyAeromoeba();
        harness.setHand(player1, List.of(new Aeromoeba(), new Aeromoeba(), new Aeromoeba()));

        activateAndDiscard();
        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void canActivateWhileTappedOnOpponentsTurn() {
        Permanent aeromoeba = addReadyAeromoeba();
        aeromoeba.setTapped(true);
        harness.setHand(player1, List.of(new Aeromoeba()));
        harness.forceActivePlayer(player2);

        activateAndDiscard();

        assertThat(gqs.getEffectivePower(gd, aeromoeba)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aeromoeba)).isEqualTo(2);
        assertThat(aeromoeba.isTapped()).isTrue();
    }

    private Permanent addReadyAeromoeba() {
        prepareAeromoebaActivation();
        return addCreatureReady(player1, new Aeromoeba());
    }

    private void prepareAeromoebaActivation() {
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
