package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkewerSlinger.class, GrizzlyBears.class})
class SkewerSlingerTest extends BaseCardTest {

    @Test
    void blockingDealsDamageToAttacker() {
        Permanent slinger = addReadySlinger(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(slinger.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void becomingBlockedDealsDamageToBlocker() {
        Permanent slinger = addReadySlinger(player1);
        slinger.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getSourcePermanentId()).isEqualTo(slinger.getId());
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());

        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void becomingBlockedByMultipleCreaturesDealsDamageToEachBlocker() {
        Permanent slinger = addReadySlinger(player1);
        slinger.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void combatTriggersAreNonTargeting() {
        Permanent slinger = addReadySlinger(player1);
        slinger.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    private Permanent addReadySlinger(Player player) {
        Permanent perm = new Permanent(new SkewerSlinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
