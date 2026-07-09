package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurtleshellChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability switches power and toughness")
    void switchesPowerAndToughness() {
        harness.addToBattlefield(player1, new TurtleshellChangeling());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent changeling = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(changeling.getEffectivePower()).isEqualTo(1);
        assertThat(changeling.getEffectiveToughness()).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.isPowerToughnessSwitched()).isTrue();
        assertThat(changeling.getEffectivePower()).isEqualTo(4);
        assertThat(changeling.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Switch wears off at cleanup")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new TurtleshellChangeling());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent changeling = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(changeling.isPowerToughnessSwitched()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.isPowerToughnessSwitched()).isFalse();
        assertThat(changeling.getEffectivePower()).isEqualTo(1);
        assertThat(changeling.getEffectiveToughness()).isEqualTo(4);
    }
}
