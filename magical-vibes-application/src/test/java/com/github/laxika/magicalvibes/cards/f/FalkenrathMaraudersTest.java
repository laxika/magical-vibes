package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalkenrathMarauders.class, SerraAngel.class, WalkingCorpse.class})
class FalkenrathMaraudersTest extends BaseCardTest {

    private Permanent addReadyMarauders() {
        return addCreatureReady(player1, new FalkenrathMarauders());
    }

    @Test
    @DisplayName("Gets two +1/+1 counters when dealing combat damage to a player")
    void getsTwoCountersOnCombatDamage() {
        Permanent marauders = addReadyMarauders();
        marauders.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat(player1); // through combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Marauders should have two +1/+1 counters
        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting counters")
    void dealsMoreDamageWithCounters() {
        Permanent marauders = addReadyMarauders();
        marauders.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // simulate having gotten counters previously
        marauders.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat(player1); // combat damage

        // 2 base power + 2 from counters = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Resolve the trigger to get two more counters.
        harness.passBothPriorities();
        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("No counters when blocked and killed")
    void noCountersWhenBlockedAndKilled() {
        Permanent marauders = addReadyMarauders();
        marauders.setAttacking(true);

        // 4/4 blocker kills the 2/2 Marauders
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1); // combat damage

        // Marauders should be dead
        harness.assertInGraveyard(player1, "Falkenrath Marauders");
    }

    @Test
    @DisplayName("Counters wait for the combat damage trigger to resolve and affect only its source")
    void countersWaitForResolutionAndOnlyAffectSource() {
        Permanent marauders = addReadyMarauders();
        Permanent other = addReadyMarauders();
        marauders.setAttacking(true);
        resolveCombat(player1);

        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zero combat damage does not trigger counters")
    void zeroCombatDamageDoesNotTrigger() {
        Permanent marauders = addReadyMarauders();
        marauders.setPowerModifier(-2);
        marauders.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending trigger does not put counters on a new permanent representing the same card")
    void pendingTriggerDoesNotAffectReturnedSource() {
        Permanent marauders = addReadyMarauders();
        marauders.setAttacking(true);
        resolveCombat(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(marauders);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, marauders.getCard());

        harness.passBothPriorities();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste allows attacking while summoning sick")
    void canAttackWhileSummoningSick() {
        Permanent marauders = harness.addToBattlefieldAndReturn(player1, new FalkenrathMarauders());
        marauders.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(marauders.isAttacking()).isTrue();
        assertThat(marauders.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void groundCreatureCannotBlock() {
        Permanent marauders = addReadyMarauders();
        addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(marauders.isAttacking()).isTrue();
    }
}
