package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskhunterBat.class})
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

    @Test
    @DisplayName("Bloodthirst adds only one counter regardless of damage amount or number of hits")
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 3);
        gd.recordDamageToPlayer(player2.getId(), 2);
        castBat();

        assertThat(findPermanent(player1, "Duskhunter Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst checks damage when the creature resolves, not when it is cast")
    void bloodthirstChecksAtEntry() {
        harness.setHand(player1, List.of(new DuskhunterBat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        gd.recordDamageToPlayer(player2.getId(), 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Duskhunter Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt after entry does not add a bloodthirst counter")
    void laterDamageDoesNotAddCounters() {
        castBat();
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Duskhunter Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Zero damage does not enable bloodthirst")
    void zeroDamageDoesNotEnableBloodthirst() {
        gd.recordDamageToPlayer(player2.getId(), 0);
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
