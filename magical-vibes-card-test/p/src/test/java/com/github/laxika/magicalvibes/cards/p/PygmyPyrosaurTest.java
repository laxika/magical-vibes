package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PygmyPyrosaur.class, GiantCockroach.class})
class PygmyPyrosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {R} gives +1/+0 until end of turn")
    void activatingAbilityBoosts() {
        Permanent pyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(1);
        assertThat(pyrosaur.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/+0")
    void canActivateMultipleTimes() {
        Permanent pyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(3);
        assertThat(pyrosaur.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent pyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(0);
        assertThat(pyrosaur.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Pygmy Pyrosaur cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new PygmyPyrosaur());

        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Can activate while tapped because the ability has no tap cost")
    void abilityDoesNotRequireTapping() {
        Permanent pyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        pyrosaur.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(1);
        assertThat(pyrosaur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent pyrosaur = harness.addToBattlefieldAndReturn(player1, new PygmyPyrosaur());
        pyrosaur.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(1);
        assertThat(pyrosaur.getToughnessModifier()).isEqualTo(0);
        assertThat(pyrosaur.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the red activation cost")
    void requiresRedMana() {
        Permanent pyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(pyrosaur.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Stacked activations boost only their source when they resolve")
    void stackedActivationsBoostOnlySource() {
        Permanent pyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        Permanent otherPyrosaur = addCreatureReady(player1, new PygmyPyrosaur());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(pyrosaur.getPowerModifier()).isEqualTo(0);

        resolveAllTriggers();

        assertThat(pyrosaur.getPowerModifier()).isEqualTo(2);
        assertThat(pyrosaur.getToughnessModifier()).isEqualTo(0);
        assertThat(otherPyrosaur.getPowerModifier()).isEqualTo(0);
        assertThat(otherPyrosaur.getToughnessModifier()).isEqualTo(0);
    }
}
