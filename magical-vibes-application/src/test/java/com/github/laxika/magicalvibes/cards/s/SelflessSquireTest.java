package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelflessSquire.class, Shock.class})
class SelflessSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage to its controller this turn and gets that many counters")
    void preventsDamageAndAddsCounters() {
        Permanent squire = castSquire();
        castShockAtPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The damage prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent squire = castSquire();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        castShockAtPlayer();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castSquire() {
        harness.setHand(player1, List.of(new SelflessSquire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Selfless Squire");
    }

    private void castShockAtPlayer() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }
}
