package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Dragonstalker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MercurialKite.class, Dragonstalker.class})
class MercurialKiteTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent kite = addCreatureReady(player1, new MercurialKite());
        kite.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Dragonstalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damaged creature untaps after its next untap step")
    void damagedCreatureUntapsAfterNextUntapStep() {
        Permanent kite = addCreatureReady(player1, new MercurialKite());
        kite.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Dragonstalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock a creature")
    void unblockedDamageDoesNotTapOrLockCreatures() {
        Permanent kite = addCreatureReady(player1, new MercurialKite());
        kite.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new Dragonstalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Tapping and preventing untapping are one combat damage trigger")
    void combatDamageCreatesOneCombinedTrigger() {
        Permanent kite = addCreatureReady(player1, new MercurialKite());
        kite.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Dragonstalker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(kite.getId());
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kite also taps and locks an attacker when dealing damage as a blocker")
    void blockingKiteLocksDamagedAttacker() {
        Permanent attacker = addCreatureReady(player1, new Dragonstalker());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MercurialKite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }
}
