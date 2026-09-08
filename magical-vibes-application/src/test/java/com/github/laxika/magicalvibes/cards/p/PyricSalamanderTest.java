package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pyric Salamander")
@CardUsed({PyricSalamander.class})
class PyricSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +1/+0 until end of turn")
    void activationBoostsPower() {
        Permanent salamander = addCreatureReady(player1, new PyricSalamander());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, salamander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, salamander)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability sacrifices it at the beginning of the next end step")
    void activationSacrificesAtNextEndStep() {
        addCreatureReady(player1, new PyricSalamander());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pyric Salamander");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pyric Salamander");
        harness.assertInGraveyard(player1, "Pyric Salamander");
    }

    @Test
    @DisplayName("Multiple activations stack their boosts and still sacrifice at the next end step")
    void multipleActivationsStackBoostsAndSacrifice() {
        Permanent salamander = addCreatureReady(player1, new PyricSalamander());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, salamander)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, salamander)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pyric Salamander");
        harness.assertInGraveyard(player1, "Pyric Salamander");
    }

    @Test
    @DisplayName("An activation during the end step waits for the following end step")
    void activationDuringEndStepWaitsForFollowingEndStep() {
        addCreatureReady(player1, new PyricSalamander());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pyric Salamander");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pyric Salamander");
        harness.assertInGraveyard(player1, "Pyric Salamander");
    }

    @Test
    @DisplayName("The ability cannot be activated without red mana")
    void cannotActivateWithoutRedMana() {
        Permanent salamander = addCreatureReady(player1, new PyricSalamander());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, salamander)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Pyric Salamander");
    }

    @Test
    @DisplayName("Without activating the ability it stays on the battlefield")
    void survivesWithoutActivation() {
        addCreatureReady(player1, new PyricSalamander());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Pyric Salamander");
    }
}
