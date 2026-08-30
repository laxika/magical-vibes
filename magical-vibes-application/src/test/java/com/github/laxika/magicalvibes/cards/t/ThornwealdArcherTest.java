package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornwealdArcher.class, AirElemental.class})
class ThornwealdArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Reach allows Thornweald Archer to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new ThornwealdArcher());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Thornweald Archer");
        harness.assertInGraveyard(player2, "Thornweald Archer");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature that Thornweald Archer damages")
    void deathtouchDestroysLargerCreature() {
        Permanent attacker = addCreatureReady(player1, new ThornwealdArcher());
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thornweald Archer");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
