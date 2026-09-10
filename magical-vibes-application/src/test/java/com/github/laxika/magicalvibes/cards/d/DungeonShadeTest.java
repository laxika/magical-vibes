package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DungeonShade.class})
class DungeonShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Dungeon Shade +1/+1 without tapping it")
    void abilityBoostsSelf() {
        Permanent shade = addCreatureReady(player1, new DungeonShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated repeatedly while mana remains")
    void abilityStacks() {
        Permanent shade = addCreatureReady(player1, new DungeonShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can be activated while Dungeon Shade is tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent shade = addCreatureReady(player1, new DungeonShade());
        shade.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can be activated with summoning sickness")
    void abilityCanBeActivatedWithSummoningSickness() {
        Permanent shade = new Permanent(new DungeonShade());
        gd.playerBattlefields.get(player1.getId()).add(shade);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability requires black mana")
    void abilityRequiresBlackMana() {
        addCreatureReady(player1, new DungeonShade());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent shade = addCreatureReady(player1, new DungeonShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability does nothing if Dungeon Shade leaves before resolution")
    void abilityDoesNothingIfSourceLeavesBeforeResolution() {
        addCreatureReady(player1, new DungeonShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
