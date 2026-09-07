package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WallOfWater.class)
class WallOfWaterTest extends BaseCardTest {

    @Test
    void defenderPreventsAttacking() {
        Permanent wall = addCreatureReady(player1, new WallOfWater());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent wall = addCreatureReady(player1, new WallOfWater());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wall.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Wall of Water")
    void resolvingAbilityBoostsPower() {
        Permanent wall = addCreatureReady(player1, new WallOfWater());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getEffectivePower()).isEqualTo(1);
        assertThat(wall.getEffectiveToughness()).isEqualTo(5);
        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new WallOfWater());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getEffectivePower()).isEqualTo(2);
        assertThat(wall.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(0);
        assertThat(wall.getToughnessModifier()).isEqualTo(0);
        assertThat(wall.getEffectivePower()).isEqualTo(0);
        assertThat(wall.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new WallOfWater());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

}
