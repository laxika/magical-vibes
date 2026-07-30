package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarnageWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 3: enters with three +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 3 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castWurm() {
        harness.setHand(player1, List.of(new CarnageWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
