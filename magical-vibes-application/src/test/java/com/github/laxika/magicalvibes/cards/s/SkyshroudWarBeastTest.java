package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshroudWarBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the opponent's nonbasic land count")
    void powerAndToughnessCountOpponentNonbasicLands() {
        Permanent warBeast = addWarBeast(player1);
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new AdarkarWastes());

        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness update as the opponent's nonbasic lands change")
    void updatesDynamically() {
        Permanent warBeast = addWarBeast(player1);
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());

        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isEqualTo(1);

        harness.addToBattlefield(player2, new AdarkarWastes());
        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(firstLand);
        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(1);
    }

    private Permanent addWarBeast(Player player) {
        Permanent permanent = new Permanent(new SkyshroudWarBeast());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
