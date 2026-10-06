package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfGlaciers.class})
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

    @Test
    @DisplayName("Ability works while tapped and summoning sick and waits for resolution")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent scion = addReadyScion();
        scion.setSummoningSick(true);
        scion.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(scion.getPowerModifier()).isZero();
        assertThat(scion.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(scion.getPowerModifier()).isEqualTo(1);
        assertThat(scion.getToughnessModifier()).isEqualTo(-1);
        assertThat(scion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Five activations cause Scion to die from zero toughness")
    void diesWhenPumpReducesToughnessToZero() {
        Permanent scion = addReadyScion();
        harness.addMana(player1, ManaColor.BLUE, 5);

        for (int activation = 0; activation < 4; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scion);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scion);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scion.getCard());
    }

    private Permanent addReadyScion() {
        return addCreatureReady(player1, new ScionOfGlaciers());
    }
}
