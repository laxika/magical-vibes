package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunbladeElfTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 without a Plains")
    void noBoostWithoutPlains() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new SunbladeElf());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 while you control a Plains")
    void boostWithPlains() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new SunbladeElf());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Plains does not turn on the boost")
    void noBoostFromOpponentPlains() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new SunbladeElf());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability gives creatures you control +1/+1 until end of turn")
    void abilityBoostsOwnCreatures() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new SunbladeElf());
        elf.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(elf.getPowerModifier()).isEqualTo(1);
        assertThat(enemyBears.getPowerModifier()).isEqualTo(0);
        assertThat(enemyBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability boost wears off at end of turn")
    void abilityBoostWearsOff() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new SunbladeElf());
        elf.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
