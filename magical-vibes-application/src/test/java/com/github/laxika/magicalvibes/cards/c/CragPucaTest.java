package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CragPucaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability switches power and toughness")
    void switchesPowerAndToughness() {
        harness.addToBattlefield(player1, new CragPuca());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent puca = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);
    }

    @Test
    @DisplayName("Switch wears off at cleanup")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new CragPuca());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent puca = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(4);
    }
}
