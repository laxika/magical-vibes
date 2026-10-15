package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarMachineLegacyOfIron.class, HydraulicHelper.class})
class WarMachineLegacyOfIronTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, another creature gets +War Machine's power/+0")
    void boostsAnotherCreatureByWarMachinesPower() {
        harness.addToBattlefield(player1, new WarMachineLegacyOfIron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target only another creature controlled by War Machine's controller")
    void targetsOnlyAnotherCreatureYouControl() {
        Permanent warMachine = harness.addToBattlefieldAndReturn(player1, new WarMachineLegacyOfIron());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HydraulicHelper());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(warMachine.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new WarMachineLegacyOfIron());
        harness.addToBattlefield(player1, new HydraulicHelper());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new WarMachineLegacyOfIron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses War Machine's power when the ability resolves")
    void usesPowerAtResolution() {
        Permanent warMachine = harness.addToBattlefieldAndReturn(player1, new WarMachineLegacyOfIron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        warMachine.setPowerModifier(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The resolved boost does not change with War Machine's power")
    void resolvedBoostStaysFixed() {
        Permanent warMachine = harness.addToBattlefieldAndReturn(player1, new WarMachineLegacyOfIron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());
        warMachine.setPowerModifier(2);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        warMachine.setPowerModifier(5);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("No legal target leaves no ability on the stack")
    void noOtherCreatureLeavesNoTriggerOnStack() {
        harness.addToBattlefield(player1, new WarMachineLegacyOfIron());
        harness.addToBattlefield(player2, new HydraulicHelper());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Uses last known power after War Machine leaves the battlefield")
    void usesLastKnownPowerWhenSourceLeaves() {
        Permanent warMachine = harness.addToBattlefieldAndReturn(player1, new WarMachineLegacyOfIron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        warMachine.setPowerModifier(4);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, warMachine));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("An ability with a removed target does not boost another creature")
    void removedTargetMakesAbilityDoNothing() {
        Permanent warMachine = harness.addToBattlefieldAndReturn(player1, new WarMachineLegacyOfIron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warMachine)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
