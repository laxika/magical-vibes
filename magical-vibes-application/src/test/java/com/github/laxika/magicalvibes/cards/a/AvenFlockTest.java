package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenFlock.class})
class AvenFlockTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Aven Flock")
    void resolvingAbilityBoostsToughness() {
        addCreatureReady(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent flock = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(flock.getEffectivePower()).isEqualTo(2);
        assertThat(flock.getEffectiveToughness()).isEqualTo(4);
        assertThat(flock.getPowerModifier()).isEqualTo(0);
        assertThat(flock.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate the ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.WHITE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent flock = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(flock.getEffectiveToughness()).isEqualTo(6);
        assertThat(flock.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent flock = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(flock.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flock.getToughnessModifier()).isEqualTo(0);
        assertThat(flock.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new AvenFlock());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost affects only the Aven Flock whose ability was activated")
    void boostAffectsOnlyItsSource() {
        Permanent flock = addCreatureReady(player1, new AvenFlock());
        Permanent otherFlock = addCreatureReady(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flock.getEffectiveToughness()).isEqualTo(4);
        assertThat(otherFlock.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate the ability while Aven Flock is tapped")
    void canActivateWhenTapped() {
        Permanent flock = addCreatureReady(player1, new AvenFlock());
        flock.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flock.isTapped()).isTrue();
        assertThat(flock.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate the ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent flock = harness.addToBattlefieldAndReturn(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flock.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability with only nonwhite mana")
    void cannotActivateWithOnlyNonWhiteMana() {
        addCreatureReady(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
