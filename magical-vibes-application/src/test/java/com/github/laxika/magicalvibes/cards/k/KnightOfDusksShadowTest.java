package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KnightOfDusksShadow.class)
class KnightOfDusksShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents opponents from gaining life but not its controller")
    void preventsOpponentsFromGainingLife() {
        addKnight();

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The activated ability gives the Knight +1/+1 until end of turn")
    void boostsSelf() {
        Permanent knight = addKnight();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent knight = addKnight();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addKnight() {
        return harness.addToBattlefieldAndReturn(player1, new KnightOfDusksShadow());
    }
}
