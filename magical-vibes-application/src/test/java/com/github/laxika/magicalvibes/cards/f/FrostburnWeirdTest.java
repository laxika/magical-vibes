package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrostburnWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Hybrid ability can be paid with blue mana")
    void pumpPaidWithBlue() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Hybrid ability can be paid with red mana")
    void pumpPaidWithRed() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Pump ability stacks across multiple activations")
    void pumpStacks() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(2);
        assertThat(weird.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(0);
        assertThat(weird.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyWeird() {
        Permanent weird = new Permanent(new FrostburnWeird());
        weird.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(weird);
        return weird;
    }
}
