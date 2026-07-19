package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScornfulAetherLichTest extends BaseCardTest {

    private Permanent addLichReady(Player player) {
        Permanent perm = new Permanent(new ScornfulAetherLich());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("{W}{B} ability grants both fear and vigilance")
    void abilityGrantsFearAndVigilance() {
        Permanent lich = addLichReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted fear and vigilance wear off at end of turn")
    void grantedKeywordsWearOff() {
        Permanent lich = addLichReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isFalse();
    }
}
