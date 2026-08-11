package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScionOfGlaciersTest extends BaseCardTest {

    @Test
    @DisplayName("{U}: gets +1/-1 until end of turn")
    void pumpGivesPlusOneMinusOne() {
        Permanent scion = addReadyScion();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scion.getPowerModifier()).isEqualTo(1);
        assertThat(scion.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Pump ability stacks across multiple activations")
    void pumpStacks() {
        Permanent scion = addReadyScion();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scion.getPowerModifier()).isEqualTo(2);
        assertThat(scion.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent scion = addReadyScion();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(scion.getPowerModifier()).isEqualTo(1);
        assertThat(scion.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(scion.getPowerModifier()).isZero();
        assertThat(scion.getToughnessModifier()).isZero();
    }

    private Permanent addReadyScion() {
        Permanent scion = new Permanent(new ScionOfGlaciers());
        scion.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(scion);
        return scion;
    }
}
