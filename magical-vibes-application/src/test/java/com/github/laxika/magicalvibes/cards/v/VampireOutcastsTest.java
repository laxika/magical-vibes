package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VampireOutcastsTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 2: enters with two +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodthirst 2: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 2 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castOutcasts() {
        harness.setHand(player1, List.of(new VampireOutcasts()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
