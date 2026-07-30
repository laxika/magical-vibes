package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuskhunterBatTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castBat();

        assertThat(findPermanent(player1, "Duskhunter Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castBat();

        assertThat(findPermanent(player1, "Duskhunter Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castBat();

        assertThat(findPermanent(player1, "Duskhunter Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBat() {
        harness.setHand(player1, List.of(new DuskhunterBat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
