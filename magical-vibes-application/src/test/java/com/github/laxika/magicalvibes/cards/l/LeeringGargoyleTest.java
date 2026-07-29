package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeeringGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Leering Gargoyle gets -2/+2 and loses flying")
    void activationSwapsStatsAndLosesFlying() {
        Permanent gargoyle = addReadyGargoyle(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent gargoyle = addReadyGargoyle(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
    }

    private Permanent addReadyGargoyle(Player player) {
        Permanent perm = new Permanent(new LeeringGargoyle());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
