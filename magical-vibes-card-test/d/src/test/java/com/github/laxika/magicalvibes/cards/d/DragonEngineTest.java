package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonEngine.class})
class DragonEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent engine = addEngine(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(1);
        assertThat(engine.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times for cumulative boost")
    void cumulativeBoost() {
        Permanent engine = addEngine(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(2);
        assertThat(engine.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate the ability while tapped because it has no tap cost")
    void canActivateWhileTapped() {
        Permanent engine = addEngine(player1);
        engine.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addEngine(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent engine = addEngine(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(0);
        assertThat(engine.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addEngine(Player player) {
        return addCreatureReady(player, new DragonEngine());
    }

    @Test
    @DisplayName("Ability can be activated with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new DragonEngine());
        engine.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(1);
        assertThat(engine.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Generic activation cost accepts mana of different colors")
    void canPayWithMixedMana() {
        Permanent engine = addEngine(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.getPowerModifier()).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability uses the stack and boosts only its own source")
    void boostsOnlyActivatingEngineOnResolution() {
        Permanent first = addEngine(player1);
        Permanent second = addEngine(player1);
        Permanent opposing = addEngine(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(second.getPowerModifier()).isEqualTo(0);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(0);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(0);
        assertThat(opposing.getPowerModifier()).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }
}
