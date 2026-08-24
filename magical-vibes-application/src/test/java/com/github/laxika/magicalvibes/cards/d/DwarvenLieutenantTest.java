package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenLieutenant.class, DwarvenArmorer.class, IcatianInfantry.class})
class DwarvenLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R}: target Dwarf creature gets +1/+0 until end of turn")
    void boostsTargetDwarf() {
        Permanent dwarf = addCreatureReady(player1, new DwarvenLieutenant());
        int originalPower = gqs.getEffectivePower(gd, dwarf);
        int originalToughness = gqs.getEffectiveToughness(gd, dwarf);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dwarf = addCreatureReady(player1, new DwarvenLieutenant());
        int originalPower = gqs.getEffectivePower(gd, dwarf);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(originalPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("The ability can target a Dwarf creature controlled by another player")
    void boostsOpponentsDwarf() {
        addCreatureReady(player1, new DwarvenLieutenant());
        Permanent dwarf = addCreatureReady(player2, new DwarvenArmorer());
        int originalPower = gqs.getEffectivePower(gd, dwarf);
        int originalToughness = gqs.getEffectiveToughness(gd, dwarf);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("The ability can target only Dwarf creatures")
    void cannotTargetNonDwarf() {
        addCreatureReady(player1, new DwarvenLieutenant());
        Permanent infantry = addCreatureReady(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, infantry.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
