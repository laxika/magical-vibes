package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({StromkirkNoble.class, SelflessCathar.class, WalkingCorpse.class, Geistflame.class})
class StromkirkNobleTest extends BaseCardTest {

    private Permanent addReadyNoble() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new StromkirkNoble());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Stromkirk Noble can't be blocked by Humans")
    void cantBeBlockedByHumans() {
        Permanent noble = addReadyNoble();
        noble.setAttacking(true);

        harness.addToBattlefield(player2, new SelflessCathar());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Stromkirk Noble can be blocked by non-Human creatures")
    void canBeBlockedByNonHumans() {
        Permanent noble = addReadyNoble();
        noble.setAttacking(true);

        harness.addToBattlefield(player2, new WalkingCorpse());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent noble = addReadyNoble();
        noble.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting a +1/+1 counter")
    void dealsMoreDamageWithCounter() {
        Permanent noble = addReadyNoble();
        noble.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        noble.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        // 1 base power + 1 from counter = 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve trigger — gets another counter
        harness.passBothPriorities();
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when blocked and killed")
    void noCounterWhenBlockedAndKilled() {
        Permanent noble = addReadyNoble();
        noble.setAttacking(true);

        // 2/2 blocker kills the 1/1 Noble
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        // Noble should be dead
        harness.assertInGraveyard(player1, "Stromkirk Noble");
    }

    @Test
    @DisplayName("No counter when combat damage hits a creature and Noble survives")
    void noCounterForCombatDamageToCreature() {
        Permanent noble = addReadyNoble();
        noble.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        noble.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(noble);
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("A pending counter trigger does not put counters on a different Noble")
    void removedSourceDoesNotGiveCounterToAnotherNoble() {
        Permanent noble = addReadyNoble();
        noble.setAttacking(true);
        Permanent otherNoble = addReadyNoble();
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player1, 0, noble.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Stromkirk Noble");
        resolveAllTriggers();

        assertThat(otherNoble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
