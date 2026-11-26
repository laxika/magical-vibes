package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NakayaShade.class)
class NakayaShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets boost when no player pays")
    void getsBoostWhenNoPlayerPays() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent shade = addShade();

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Any player can pay to prevent the boost")
    void anyPlayerCanPayToPreventTheBoost() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent shade = addShade();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The other player is still asked after the current player paid, and the Shade gets no boost")
    void currentPlayerPaysAndTheOtherPlayerIsStillAsked() {
        Permanent shade = addShade();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        giveManaSource(player2);

        activateAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class))
                .isNotNull()
                .satisfies(asked -> assertThat(asked.playerId()).isEqualTo(player2.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the activated shade gets boosted")
    void onlyTheActivatedShadeGetsBoosted() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent shade = addShade();
        Permanent otherShade = harness.addToBattlefieldAndReturn(player1, new NakayaShade());

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherShade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherShade)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosts source even if it changes controller before resolution")
    void boostsSourceEvenIfItChangesControllerBeforeResolution() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent shade = addShade();

        activateAbilityWithoutResolving();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), shade,
                        new GainControlOfTargetEffect(Duration.PERMANENT),
                        Duration.PERMANENT, null, "Test setup"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent shade = addShade();

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations give cumulative boosts")
    void repeatedActivationsGiveCumulativeBoosts() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent shade = addShade();

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(3);
    }

    @Test
    void opponentIsAskedFirstOnTheirTurnAndCanPayWithColoredMana() {
        Permanent shade = addShade();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        activateAbility();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addShade() {
        return harness.addToBattlefieldAndReturn(player1, new NakayaShade());
    }

    private void activateAbility() {
        activateAbilityWithoutResolving();
        harness.passBothPriorities();
    }

    private void activateAbilityWithoutResolving() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
    }
}
