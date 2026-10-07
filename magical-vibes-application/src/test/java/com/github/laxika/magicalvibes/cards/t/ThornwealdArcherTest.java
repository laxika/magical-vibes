package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LucentLiminid;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornwealdArcher.class, LucentLiminid.class, NessianCourser.class})
class ThornwealdArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Reach does not prevent a ground creature from blocking Thornweald Archer")
    void reachDoesNotGrantFlyingEvasion() {
        addCreatureReady(player1, new ThornwealdArcher());
        addCreatureReady(player2, new NessianCourser());
        int defenderLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Thornweald Archer");
        harness.assertInGraveyard(player2, "Nessian Courser");
        harness.assertLife(player2, defenderLife);
    }

    @Test
    @DisplayName("Reach allows Thornweald Archer to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new LucentLiminid());
        addCreatureReady(player2, new ThornwealdArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Thornweald Archer");
        harness.assertInGraveyard(player2, "Thornweald Archer");
        harness.assertInGraveyard(player1, "Lucent Liminid");
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature that Thornweald Archer damages")
    void deathtouchDestroysLargerCreature() {
        Permanent attacker = addCreatureReady(player1, new ThornwealdArcher());
        Permanent blocker = addCreatureReady(player2, new LucentLiminid());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Thornweald Archer");
        harness.assertInGraveyard(player2, "Lucent Liminid");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
