package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CylianSunsingerTest extends BaseCardTest {

    private void addRgwMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    // ===== Ability boosts this creature and each other same-name creature =====

    @Test
    @DisplayName("Ability gives +3/+3 to itself and every creature with the same name, on any side")
    void boostsSelfAndAllSameNameCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent ownCopy = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppCopy = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        harness.forceActivePlayer(player1);
        addRgwMana(player1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        // Source and every other same-name creature (both controllers) get +3/+3
        assertThat(source.getEffectivePower()).isEqualTo(5);
        assertThat(source.getEffectiveToughness()).isEqualTo(5);
        assertThat(ownCopy.getEffectivePower()).isEqualTo(5);
        assertThat(ownCopy.getEffectiveToughness()).isEqualTo(5);
        assertThat(oppCopy.getEffectivePower()).isEqualTo(5);
        assertThat(oppCopy.getEffectiveToughness()).isEqualTo(5);

        // Different-named creature is untouched
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Boost wears off at end of turn =====

    @Test
    @DisplayName("The +3/+3 boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());

        harness.forceActivePlayer(player1);
        addRgwMana(player1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(0);
        assertThat(source.getEffectivePower()).isEqualTo(2);
    }
}
