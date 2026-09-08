package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyrQuadropodTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability switches power and toughness")
    void switchesPowerAndToughness() {
        harness.addToBattlefield(player1, new MyrQuadropod());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent myr = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    @Test
    @DisplayName("The switch wears off at cleanup")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new MyrQuadropod());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent myr = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(4);
    }
}
