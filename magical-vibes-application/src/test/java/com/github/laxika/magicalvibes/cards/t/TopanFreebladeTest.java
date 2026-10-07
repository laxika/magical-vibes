package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TopanFreeblade.class, HitchclawRecluse.class})
class TopanFreebladeTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to a player makes it renowned with a +1/+1 counter")
    void becomesRenowned() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());

        attackUnblocked();

        assertThat(freeblade.isRenowned()).isTrue();
        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vigilance keeps it untapped when attacking")
    void vigilanceKeepsItUntapped() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(freeblade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Renown only applies once")
    void renownAppliesOnce() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        freeblade.setRenowned(true);

        attackUnblocked();

        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A blocked Topan Freeblade never becomes renowned")
    void blockedDoesNotBecomeRenowned() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        addCreatureReady(player2, new HitchclawRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(freeblade.isRenowned()).isFalse();
        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Zero combat damage does not trigger renown")
    void zeroCombatDamageDoesNotMakeRenowned() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        freeblade.setPowerModifier(-2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        attackUnblocked();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(freeblade.isRenowned()).isFalse();
        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing the renown counter does not allow renown to happen again")
    void remainsRenownedAfterCounterRemoved() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        attackUnblocked();
        assertThat(freeblade.isRenowned()).isTrue();
        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        freeblade.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        attackUnblocked();

        assertThat(freeblade.isRenowned()).isTrue();
        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void attackUnblocked() {
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }
}
