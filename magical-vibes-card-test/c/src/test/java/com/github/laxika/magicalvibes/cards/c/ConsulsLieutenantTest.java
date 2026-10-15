package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsulsLieutenant.class, GrizzlyBears.class})
class ConsulsLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking while not renowned does not pump other attackers")
    void noPumpWhenNotRenowned() {
        addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking while renowned gives other attacking creatures +1/+1")
    void pumpsOtherAttackersWhenRenowned() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(lieutenant.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A non-attacking creature you control is not pumped")
    void doesNotPumpNonAttackers() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOff() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal damage and does not grant renown")
    void killsBlockerWithoutBecomingRenowned() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lieutenant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife);
        assertThat(lieutenant.isRenowned()).isFalse();
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two renowned Lieutenants each boost the other, but not themselves or opposing creatures")
    void multipleLieutenantsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent second = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent opponent = addCreatureReady(player2, new ConsulsLieutenant());
        first.setRenowned(true);
        second.setRenowned(true);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Renown from first-strike damage does not retroactively boost fellow attackers")
    void becomingRenownedDuringCombatDoesNotBoostAttackers() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent other = addCreatureReady(player1, new ConsulsLieutenant());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(lieutenant.isRenowned()).isTrue();
        assertThat(other.isRenowned()).isTrue();
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.getPowerModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife - 4);
    }

    @Test
    @DisplayName("A renowned source leaving the battlefield does not stop its attack trigger")
    void attackTriggerUsesLastKnownRenown() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent other = addCreatureReady(player1, new ConsulsLieutenant());
        addCreatureReady(player2, new ConsulsLieutenant());
        lieutenant.setRenowned(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(gd.stack).isNotEmpty();
            gd.playerBattlefields.get(player1.getId()).remove(lieutenant);
            gd.playerGraveyards.get(player1.getId()).add(lieutenant.getCard());
            resolveAllTriggers();

            assertThat(other.getPowerModifier()).isEqualTo(1);
            assertThat(other.getToughnessModifier()).isEqualTo(1);
        });
    }
}
