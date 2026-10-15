package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LurkingCrocodile.class, Island.class, RuneclawBear.class})
class LurkingCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castCrocodile();

        assertThat(findPermanent(player1, "Lurking Crocodile")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castCrocodile();

        assertThat(findPermanent(player1, "Lurking Crocodile")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castCrocodile();

        assertThat(findPermanent(player1, "Lurking Crocodile")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 5);
        castCrocodile();
        assertThat(findPermanent(player1, "Lurking Crocodile")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void bloodthirstChecksAtEntry() {
        harness.castFromHand(player1, new LurkingCrocodile(), "{2}{G}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Lurking Crocodile")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void laterDamageDoesNotGrantCounter() {
        castCrocodile();
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Lurking Crocodile")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void islandwalkPreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new LurkingCrocodile());
        attacker.setAttacking(true);
        addCreatureReady(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new Island());
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void islandwalkChecksDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new LurkingCrocodile());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castCrocodile() {
        harness.castFromHand(player1, new LurkingCrocodile(), "{2}{G}");
        resolveAllTriggers();
    }
}
