package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyphoidRats.class, FortressCrab.class})
class TyphoidRatsTest extends BaseCardTest {

    @Test
    void canCastAndResolve() {
        harness.castFromHand(player1, new TyphoidRats(), "{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Typhoid Rats");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void destroysHighToughnessBlockerEvenWhenRatsDie() {
        addCreatureReady(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new FortressCrab());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Fortress Crab");
        harness.assertNotOnBattlefield(player1, "Typhoid Rats");
        harness.assertNotOnBattlefield(player2, "Fortress Crab");
    }

    @Test
    void destroysHighToughnessAttackerWhenBlocking() {
        addCreatureReady(player1, new FortressCrab());
        harness.addToBattlefield(player2, new TyphoidRats());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Fortress Crab");
        harness.assertInGraveyard(player2, "Typhoid Rats");
        harness.assertNotOnBattlefield(player1, "Fortress Crab");
        harness.assertNotOnBattlefield(player2, "Typhoid Rats");
    }

    @Test
    void unblockedDamageOnlyReducesDefendingPlayersLife() {
        addCreatureReady(player1, new TyphoidRats());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Typhoid Rats");
    }
}
