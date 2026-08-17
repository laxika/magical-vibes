package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R}: target Dwarf creature gets +1/+0 until end of turn")
    void boostsTargetDwarf() {
        Permanent dwarf = addCreatureReady(player1, new DwarvenLieutenant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dwarf = addCreatureReady(player1, new DwarvenLieutenant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, dwarf.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can target only Dwarf creatures")
    void cannotTargetNonDwarf() {
        addCreatureReady(player1, new DwarvenLieutenant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
