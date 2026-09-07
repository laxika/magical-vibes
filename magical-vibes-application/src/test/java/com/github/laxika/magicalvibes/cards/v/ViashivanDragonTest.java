package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ViashivanDragon.class)
class ViashivanDragonTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void redPumpBoostsPower() {
        Permanent dragon = addCreatureReady(player1, new ViashivanDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{G}: gets +0/+1 until end of turn")
    void greenPumpBoostsToughness() {
        Permanent dragon = addCreatureReady(player1, new ViashivanDragon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A pump affects only the dragon that activated it")
    void pumpAffectsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new ViashivanDragon());
        Permanent otherDragon = addCreatureReady(player2, new ViashivanDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(otherDragon.getPowerModifier()).isZero();
        assertThat(otherDragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Both pumps can be activated multiple times")
    void bothPumpsStack() {
        Permanent dragon = addCreatureReady(player1, new ViashivanDragon());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        assertThat(dragon.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Pumps wear off at end of turn")
    void pumpsWearOffAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new ViashivanDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }
}
