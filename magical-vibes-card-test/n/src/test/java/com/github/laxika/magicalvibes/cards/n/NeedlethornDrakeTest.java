package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeedlethornDrake.class, GrizzlyBears.class, GiantSpider.class})
class NeedlethornDrakeTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent drake = addCreatureReady(player1, new NeedlethornDrake());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(drake)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drake);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchDestroysLargerCreatureInCombat() {
        Permanent drake = addCreatureReady(player2, new NeedlethornDrake());
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(drake);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Needlethorn Drake");
    }

    @Test
    void canBeBlockedByCreatureWithReachAndKillsItWithDeathtouch() {
        Permanent drake = addCreatureReady(player1, new NeedlethornDrake());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(drake)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(spider),
                gd.playerBattlefields.get(player1.getId()).indexOf(drake))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Needlethorn Drake");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertLife(player2, 20);
    }

    @Test
    void canBeBlockedByAnotherFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new NeedlethornDrake());
        Permanent blocker = addCreatureReady(player2, new NeedlethornDrake());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Needlethorn Drake");
        harness.assertInGraveyard(player2, "Needlethorn Drake");
        harness.assertLife(player2, 20);
    }
}
