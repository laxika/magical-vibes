package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandstoneWarrior.class, TrainedArmodon.class})
class SandstoneWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Sandstone Warrior")
    void resolvingAbilityBoostsPower() {
        Permanent warrior = addCreatureReady(player1, new SandstoneWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(1);
        assertThat(warrior.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost uses the stack and affects only the activating warrior")
    void boostWaitsForResolutionAndOnlyAffectsItsSource() {
        Permanent otherWarrior = addCreatureReady(player1, new SandstoneWarrior());
        Permanent warrior = addCreatureReady(player1, new SandstoneWarrior());
        Permanent opposingWarrior = addCreatureReady(player2, new SandstoneWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(warrior.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(1);
        assertThat(warrior.getToughnessModifier()).isZero();
        assertThat(otherWarrior.getPowerModifier()).isZero();
        assertThat(opposingWarrior.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick warrior can activate its mana-only ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new SandstoneWarrior());
        warrior.setSummoningSick(true);
        warrior.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(1);
        assertThat(warrior.getToughnessModifier()).isZero();
        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent warrior = addCreatureReady(player1, new SandstoneWarrior());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new SandstoneWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("First strike lets a pumped warrior survive combat with an equal-sized blocker")
    void firstStrikeDealsDamageBeforeRegularDamage() {
        Permanent warrior = addCreatureReady(player1, new SandstoneWarrior());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        warrior.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Sandstone Warrior");
        harness.assertInGraveyard(player2, "Trained Armodon");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new SandstoneWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot pay the red activation cost with mana of another color")
    void cannotActivateWithWrongColorMana() {
        addCreatureReady(player1, new SandstoneWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
