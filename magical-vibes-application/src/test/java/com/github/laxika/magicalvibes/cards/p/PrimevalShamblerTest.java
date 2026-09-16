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

@CardUsed({PrimevalShambler.class})
class PrimevalShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/+1 until end of turn")
    void activatingAbilityBoosts() {
        Permanent shambler = addCreatureReady(player1, new PrimevalShambler());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(1);
        assertThat(shambler.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/+1")
    void canActivateMultipleTimes() {
        Permanent shambler = addCreatureReady(player1, new PrimevalShambler());
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(shambler.getPowerModifier()).isEqualTo(3);
        assertThat(shambler.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent shambler = addCreatureReady(player1, new PrimevalShambler());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(0);
        assertThat(shambler.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new PrimevalShambler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new PrimevalShambler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability has no tap cost")
    void canActivateWithSummoningSickness() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new PrimevalShambler());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(1);
        assertThat(shambler.getToughnessModifier()).isEqualTo(1);
    }
}
