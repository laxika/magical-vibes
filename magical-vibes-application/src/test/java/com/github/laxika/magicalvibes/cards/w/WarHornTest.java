package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarHornTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void boostsOwnAttackingCreatures() {
        harness.addToBattlefield(player1, new WarHorn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        markAttacking(player1, List.of(1));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not get the boost")
    void nonAttackingCreaturesNotBoosted() {
        harness.addToBattlefield(player1, new WarHorn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not get the boost")
    void opponentAttackersNotBoosted() {
        harness.addToBattlefield(player1, new WarHorn());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        markAttacking(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    private void markAttacking(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int idx : attackerIndices) {
            battlefield.get(idx).setAttacking(true);
        }
    }
}
