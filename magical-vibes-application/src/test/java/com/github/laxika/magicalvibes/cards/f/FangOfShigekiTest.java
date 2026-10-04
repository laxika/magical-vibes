package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GreaterTanuki;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangOfShigeki.class, GreaterTanuki.class})
class FangOfShigekiTest extends BaseCardTest {

    @Test
    void deathtouchKillsLargerCreatureInCombat() {
        Permanent fang = addCreatureReady(player1, new FangOfShigeki());
        Permanent blocker = addCreatureReady(player2, new GreaterTanuki());
        fang.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fang);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fang.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    void deathtouchKillsTramplingAttackerWhileExcessDamageReachesPlayer() {
        Permanent attacker = addCreatureReady(player1, new GreaterTanuki());
        Permanent fang = addCreatureReady(player2, new FangOfShigeki());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(fang);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greater Tanuki");
        harness.assertInGraveyard(player2, "Fang of Shigeki");
        harness.assertNotOnBattlefield(player1, "Greater Tanuki");
        harness.assertNotOnBattlefield(player2, "Fang of Shigeki");
        harness.assertLife(player2, 15);
    }
}
