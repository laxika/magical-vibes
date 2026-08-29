package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TangleAspTest extends BaseCardTest {

    @Test
    @DisplayName("When Tangle Asp becomes blocked, it destroys the blocker at end of combat")
    void becomesBlockedDestroysBlockerAtEndOfCombat() {
        Permanent asp = addReadyAsp(player1);
        asp.setAttacking(true);
        addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Tangle Asp blocks, it destroys the attacker at end of combat")
    void blocksDestroysAttackerAtEndOfCombat() {
        Permanent spider = addReadySpider(player1);
        spider.setAttacking(true);
        addReadyAsp(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    private Permanent addReadyAsp(Player player) {
        Permanent perm = new Permanent(new TangleAsp());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
