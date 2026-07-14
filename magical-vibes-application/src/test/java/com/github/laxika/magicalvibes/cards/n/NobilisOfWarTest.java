package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NobilisOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +2/+0, including Nobilis itself")
    void boostsAttackingCreatures() {
        Permanent nobilis = addCreatureReady(player1, new NobilisOfWar()); // 3/4
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        declareAttackers(player1, List.of(0, 1));

        // Nobilis (3/4) attacking gets +2/+0 from its own static effect = 5/4
        assertThat(gqs.getEffectivePower(gd, nobilis)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nobilis)).isEqualTo(4);

        // Grizzly Bears (2/2) attacking gets +2/+0 = 4/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not get the boost")
    void nonAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new NobilisOfWar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        // Only Nobilis attacks (index 0), not bears
        declareAttackers(player1, List.of(0));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not get the boost")
    void opponentAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new NobilisOfWar());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        declareAttackers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int idx : attackerIndices) {
            battlefield.get(idx).setAttacking(true);
        }
    }

}
