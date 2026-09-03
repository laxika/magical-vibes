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

@CardUsed(PearlDragon.class)
class PearlDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Pearl Dragon")
    void resolvingAbilityBoostsToughness() {
        Permanent dragon = addCreatureReady(player1, new PearlDragon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate the ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent dragon = addCreatureReady(player1, new PearlDragon());
        harness.addMana(player1, ManaColor.WHITE, 6);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(dragon.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate the ability without tapping Pearl Dragon")
    void abilityDoesNotRequireTapping() {
        Permanent dragon = addCreatureReady(player1, new PearlDragon());
        dragon.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new PearlDragon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new PearlDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability without white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new PearlDragon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
