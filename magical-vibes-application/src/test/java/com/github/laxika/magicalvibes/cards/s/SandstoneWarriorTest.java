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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
}
