package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrneryGoblin.class, VernadiShieldmate.class})
class OrneryGoblinTest extends BaseCardTest {

    @Test
    void blockingDealsDamageToAttacker() {
        Permanent goblin = addCreatureReady(player2, new OrneryGoblin());
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(goblin.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vernadi Shieldmate").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void becomingBlockedDealsDamageToBlocker() {
        Permanent goblin = addCreatureReady(player1, new OrneryGoblin());
        goblin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VernadiShieldmate());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getSourcePermanentId()).isEqualTo(goblin.getId());
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Vernadi Shieldmate").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void becomingBlockedByMultipleCreaturesDealsDamageToEachBlocker() {
        Permanent goblin = addCreatureReady(player1, new OrneryGoblin());
        goblin.setAttacking(true);
        addCreatureReady(player2, new VernadiShieldmate());
        addCreatureReady(player2, new VernadiShieldmate());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Vernadi Shieldmate"))
                .hasSize(2)
                .allMatch(permanent -> permanent.getMarkedDamage() == 1);
    }

    @Test
    void combatTriggersAreNonTargeting() {
        Permanent goblin = addCreatureReady(player1, new OrneryGoblin());
        goblin.setAttacking(true);
        addCreatureReady(player2, new VernadiShieldmate());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    @Test
    void opposingGoblinsKillEachOtherEvenAfterOneTriggerSourceDies() {
        Permanent attacker = addCreatureReady(player1, new OrneryGoblin());
        attacker.setAttacking(true);
        addCreatureReady(player2, new OrneryGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(StackEntry::isNonTargeting);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ornery Goblin")).isZero();
        assertThat(countPermanents(player2, "Ornery Goblin")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(countPermanents(player2, "Ornery Goblin")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof OrneryGoblin);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof OrneryGoblin);
    }

    @Test
    void unblockedGoblinDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new OrneryGoblin());
        attacker.setAttacking(true);
        addCreatureReady(player2, new VernadiShieldmate());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Vernadi Shieldmate").getMarkedDamage()).isZero();
    }
}
