package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KillerBees.class)
class KillerBeesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Killer Bees")
    void resolvingAbilityBoosts() {
        Permanent bees = addCreatureReady(player1, new KillerBees());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(1);
        assertThat(bees.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent bees = addCreatureReady(player1, new KillerBees());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(3);
        assertThat(bees.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate while summoning sick because its cost does not tap it")
    void canActivateWhileSummoningSick() {
        Permanent bees = harness.addToBattlefieldAndReturn(player1, new KillerBees());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(1);
        assertThat(bees.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent bees = addCreatureReady(player1, new KillerBees());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bees.getPowerModifier()).isEqualTo(0);
        assertThat(bees.getToughnessModifier()).isEqualTo(0);
        assertThat(bees.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new KillerBees());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

}
