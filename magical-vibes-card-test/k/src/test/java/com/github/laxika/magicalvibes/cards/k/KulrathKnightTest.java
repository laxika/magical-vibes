package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KulrathKnight.class, SafeholdElite.class})
class KulrathKnightTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature with a counter can't attack while Kulrath Knight is out")
    void opponentCounteredCreatureCannotAttack() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature without counters can still attack")
    void opponentCreatureWithoutCountersCanAttack() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        declareAttackers(player2, List.of(index));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Kulrath's controller's own countered creatures can still attack")
    void ownCounteredCreatureCanAttack() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(index));

        // Safehold Elite plus a +1/+1 counter deals 3 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opponent's creature with a counter can't block while Kulrath Knight is out")
    void opponentCounteredCreatureCannotBlock() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);

        Permanent blocker = addCreature(player2);
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        prepareDeclareBlockers();

        // Kulrath is at attacker-battlefield index 0; Safehold Elite is at index 1.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature without counters can still block")
    void opponentCreatureWithoutCountersCanBlock() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);

        addCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    void chargeCounterPreventsAttacking() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);
        attacker.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chargeCounterPreventsBlocking() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCreature(player2);
        blocker.setCounterCount(CounterType.CHARGE, 1);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastCounterAllowsAttacking() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setCounterCount(CounterType.CHARGE, 0);

        declareAttackers(player2, List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void removingKnightAllowsCounteredCreatureToAttack() {
        Permanent knight = addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).remove(knight);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void losingAbilitiesAllowsCounteredCreatureToBlock() {
        Permanent knight = addReadyKulrath(player1);
        knight.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCreature(player2);
        blocker.setCounterCount(CounterType.CHARGE, 1);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    void ownCounteredCreatureCanBlock() {
        addReadyKulrath(player2);
        Permanent blocker = addCreature(player2);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    void flyingPreventsGroundCreatureFromBlockingKnight() {
        addReadyKulrath(player1);
        addCreature(player2);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictionFollowsKnightsCurrentController() {
        Permanent knight = addReadyKulrath(player1);
        gd.playerBattlefields.get(player1.getId()).remove(knight);
        gd.playerBattlefields.get(player2.getId()).add(knight);
        Permanent attacker = addCreature(player1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void witherDealsNormalDamageToPlayers() {
        addReadyKulrath(player1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void witherDamageKillsPersistCreatureWithoutReturningIt() {
        Permanent attacker = addCreature(player1);
        addReadyKulrath(player2);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Safehold Elite");
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
    }

    private Permanent addReadyKulrath(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KulrathKnight());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addCreature(Player controller) {
        Permanent perm = harness.addToBattlefieldAndReturn(controller, new SafeholdElite());
        perm.setSummoningSick(false);
        return perm;
    }
}
