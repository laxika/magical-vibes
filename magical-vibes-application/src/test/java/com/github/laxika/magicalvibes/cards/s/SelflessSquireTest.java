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
        harness.passBothPriorities();

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
        harness.castAndResolveInstant(player2, 0, player1.getId());
    }

    @Test
    void countersUseTheStackAfterDamageIsPrevented() {
        Permanent squire = castSquire();
        castShockAtPlayer();

        harness.assertLife(player1, 20);
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void preventionContinuesAfterSquireDies() {
        Permanent squire = castSquire();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, squire.getId());
        harness.assertInGraveyard(player1, "Selfless Squire");

        castShockAtPlayer();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventionResolvesEvenIfSquireDiesInResponseToItsEnterTrigger() {
        harness.setHand(player1, List.of(new SelflessSquire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent squire = findPermanent(player1, "Selfless Squire");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, squire.getId());
        harness.assertInGraveyard(player1, "Selfless Squire");
        harness.passBothPriorities();

        castShockAtPlayer();

        harness.assertLife(player1, 20);
    }

    @Test
    void everySquireTriggersForTheSamePreventedDamage() {
        Permanent first = castSquire();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SelflessSquire());

        castShockAtPlayer();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
