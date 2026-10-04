package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DriftingShade.class, RuneclawBear.class})
class DriftingShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Drifting Shade puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new DriftingShade()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drifting Shade");
    }

    @Test
    @DisplayName("Activating the ability gives +1/+1 and does not tap the creature")
    void abilityBoostsSelf() {
        addCreatureReady(player1, new DriftingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated repeatedly while mana remains")
    void abilityStacks() {
        addCreatureReady(player1, new DriftingShade());
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(4);
        assertThat(shade.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DriftingShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability resolves without a boost if Drifting Shade leaves the battlefield first")
    void abilityHasNoEffectIfSourceRemoved() {
        addCreatureReady(player1, new DriftingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without black mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new DriftingShade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Shade can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new DriftingShade());
        shade.setSummoningSick(true);
        shade.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only its source")
    void boostUsesStackAndOnlyAffectsSource() {
        Permanent shade = addCreatureReady(player1, new DriftingShade());
        Permanent otherShade = addCreatureReady(player1, new DriftingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(otherShade.getEffectivePower()).isEqualTo(1);
        assertThat(otherShade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("White mana cannot pay the black activation cost")
    void cannotActivateWithWrongColorMana() {
        addCreatureReady(player1, new DriftingShade());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Drifting Shade")
    void groundCreatureCannotBlockShade() {
        addCreatureReady(player1, new DriftingShade());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
