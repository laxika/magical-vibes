package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CitadelCastellan.class, YokedOx.class})
class CitadelCastellanTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 2 puts two +1/+1 counters on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent castellan = addCreatureReady(player1, new CitadelCastellan());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(castellan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(castellan.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent castellan = addCreatureReady(player1, new CitadelCastellan());
        castellan.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(castellan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Renown does not trigger when the creature is blocked")
    void noRenownWhenBlocked() {
        Permanent castellan = addCreatureReady(player1, new CitadelCastellan());
        addCreatureReady(player2, new YokedOx());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(castellan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(castellan.isRenowned()).isFalse();
    }

    @Test
    @DisplayName("Vigilance keeps Citadel Castellan untapped when it attacks")
    void vigilanceKeepsAttackerUntapped() {
        Permanent castellan = addCreatureReady(player1, new CitadelCastellan());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(castellan.isAttacking()).isTrue();
        assertThat(castellan.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dealing combat damage to a creature while blocking does not cause renown")
    void blockingDoesNotCauseRenown() {
        addCreatureReady(player1, new YokedOx());
        Permanent castellan = addCreatureReady(player2, new CitadelCastellan());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(castellan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(castellan.isRenowned()).isFalse();
    }
}
