package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FurybornHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 6: enters with six +1/+1 counters when an opponent was dealt any damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bloodthirst 6: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 6 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 5);
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castHellkite() {
        harness.setHand(player1, List.of(new FurybornHellkite()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
