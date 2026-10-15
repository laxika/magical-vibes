package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonWhelp.class})
class DragonWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/+0")
    void activatingAbilityBoostsPower() {
        Permanent whelp = addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(1);
        assertThat(whelp.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/+0")
    void canActivateMultipleTimes() {
        Permanent whelp = addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(whelp.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Activation count resets at the start of the next turn")
    void activationCountResetsAtStartOfNextTurn() {
        addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.CLEANUP);
        harness.handleCardChosen(player2, 0);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four or more times")
    void sacrificedWhenActivatedFourOrMoreTimes() {
        addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        // Move to end step
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        // End step trigger fires and condition IS met
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated more than four times")
    void sacrificedWhenActivatedMoreThanFourTimes() {
        addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 5);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        // Move to end step
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Activating four times during the end step sacrifices at the next end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        addCreatureReady(player1, new DragonWhelp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent whelp = addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(0);
        assertThat(whelp.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Separate Dragon Whelps track their own activations")
    void activationCountsAreSeparateForEachWhelp() {
        Permanent first = addCreatureReady(player1, new DragonWhelp());
        Permanent second = addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.RED, 6);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(first.getPowerModifier()).isEqualTo(3);
        assertThat(second.getPowerModifier()).isEqualTo(3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Dragon Whelp")).containsExactly(first, second);
    }

    @Test
    @DisplayName("Firebreathing can be activated while tapped and summoning sick")
    void abilityDoesNotRequireUntappedOrReadyCreature() {
        Permanent whelp = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        whelp.setSummoningSick(true);
        whelp.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(whelp.getPowerModifier()).isEqualTo(1);
        assertThat(whelp.getToughnessModifier()).isEqualTo(0);
        assertThat(whelp.isTapped()).isTrue();
    }

}
