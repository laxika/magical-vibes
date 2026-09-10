package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneShambler.class})
class FlowstoneShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoosts() {
        Permanent shambler = addCreatureReady(player1, new FlowstoneShambler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shambler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shambler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations lower toughness to 0, destroying it")
    void repeatedActivationsCanKillIt() {
        addCreatureReady(player1, new FlowstoneShambler());
        harness.addMana(player1, ManaColor.RED, 2);

        // 2/2 -> +1/-1 -> 3/1, then +1/-1 again -> 4/0 -> dies to state-based action.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flowstone Shambler");
        harness.assertInGraveyard(player1, "Flowstone Shambler");
    }

    @Test
    @DisplayName("The ability requires red mana")
    void abilityRequiresRedMana() {
        addCreatureReady(player1, new FlowstoneShambler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent shambler = addCreatureReady(player1, new FlowstoneShambler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shambler)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shambler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shambler)).isEqualTo(2);
    }
}
