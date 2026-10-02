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

@CardUsed(FoxfireOak.class)
class FoxfireOakTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +3/+0 (paid with green mana)")
    void activatingAbilityBoostsPowerWithGreen() {
        Permanent oak = addCreatureReady(player1, new FoxfireOak());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can also be paid with red mana (hybrid)")
    void activatingAbilityBoostsPowerWithRed() {
        Permanent oak = addCreatureReady(player1, new FoxfireOak());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Hybrid ability can be paid with a mix of red and green mana")
    void activatingAbilityWithMixedHybridManaBoostsPower() {
        Permanent oak = addCreatureReady(player1, new FoxfireOak());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent oak = addCreatureReady(player1, new FoxfireOak());
        oak.tap();
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate the ability with fewer than three hybrid mana")
    void cannotActivateWithoutEnoughHybridMana() {
        addCreatureReady(player1, new FoxfireOak());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +3/+0")
    void canActivateMultipleTimes() {
        Permanent oak = addCreatureReady(player1, new FoxfireOak());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(6);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent oak = addCreatureReady(player1, new FoxfireOak());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
    }

}
