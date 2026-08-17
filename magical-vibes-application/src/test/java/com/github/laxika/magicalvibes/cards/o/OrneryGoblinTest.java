package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrneryGoblinTest extends BaseCardTest {

    @Test
    void blockingDealsDamageToAttacker() {
        Permanent goblin = addReadyGoblin(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(goblin.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void becomingBlockedDealsDamageToBlocker() {
        Permanent goblin = addReadyGoblin(player1);
        goblin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getSourcePermanentId()).isEqualTo(goblin.getId());
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void becomingBlockedByMultipleCreaturesDealsDamageToEachBlocker() {
        Permanent goblin = addReadyGoblin(player1);
        goblin.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears"))
                .allMatch(permanent -> permanent.getMarkedDamage() == 1);
    }

    @Test
    void combatTriggersAreNonTargeting() {
        Permanent goblin = addReadyGoblin(player1);
        goblin.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent perm = new Permanent(new OrneryGoblin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
