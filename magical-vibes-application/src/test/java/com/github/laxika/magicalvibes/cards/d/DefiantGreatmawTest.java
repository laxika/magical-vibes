package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefiantGreatmawTest extends BaseCardTest {

    /** Drives the stack to completion. Bounded so a stuck state fails fast instead of hanging. */
    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty(); guard++) {
            harness.passBothPriorities();
        }
    }

    // ===== ETB: two -1/-1 counters on target creature you control =====

    @Test
    @DisplayName("ETB puts two -1/-1 counters on a creature you control")
    void etbPutsTwoCountersOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new DefiantGreatmaw()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 0, elemental.getId(), null);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Air Elemental (4/4) with two -1/-1 counters → 2/2.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB cannot target a creature you don't control")
    void etbCannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DefiantGreatmaw()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentCreature, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    // ===== Self-trigger: you put -1/-1 counters on it → remove one from another creature you control =====

    @Test
    @DisplayName("When you put -1/-1 counters on it, remove a -1/-1 counter from another target creature you control")
    void selfCounterTriggerRemovesCounterFromAnotherOwnCreature() {
        Permanent greatmaw = harness.addToBattlefieldAndReturn(player1, new DefiantGreatmaw());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1); // 2/2 → 1/1

        // player1's Skinrender puts three -1/-1 counters on Greatmaw (4/5 → 1/2, survives) → self-trigger.
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 0, greatmaw.getId(), null);
        harness.passBothPriorities(); // resolve Skinrender spell → its ETB on stack
        harness.passBothPriorities(); // resolve ETB → counters on Greatmaw → self-trigger asks for a target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities(); // resolve Greatmaw's trigger → remove one -1/-1 counter from the bear

        assertThat(greatmaw.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger it")
    void doesNotTriggerWhenOpponentPlacesCounters() {
        Permanent greatmaw = harness.addToBattlefieldAndReturn(player1, new DefiantGreatmaw());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        // player2's Skinrender puts the counters on player1's Greatmaw → "you put …" does not fire.
        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        gs.playCard(gd, player2, 0, 0, greatmaw.getId(), null);
        resolveStack();

        assertThat(greatmaw.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        // No target prompt was raised — the trigger did not fire for the opponent's counters.
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
