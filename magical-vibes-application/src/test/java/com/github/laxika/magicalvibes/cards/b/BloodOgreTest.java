package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castOgre() {
        harness.setHand(player1, List.of(new BloodOgre()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
