package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColosYearlingTest extends BaseCardTest {

    @Test
    @DisplayName("{R} gives Colos Yearling +1/+0 until end of turn")
    void pumpAbilityBoostsPower() {
        Permanent yearling = addReadyYearling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colos Yearling's pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent yearling = addReadyYearling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(1);
    }

    private Permanent addReadyYearling(Player player) {
        Permanent permanent = new Permanent(new ColosYearling());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
