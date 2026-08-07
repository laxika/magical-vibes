package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RhoxMaulersTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 2 puts two +1/+1 counters on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(maulers.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());
        maulers.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A blocker that soaks all the damage leaves no trample damage and no renown")
    void fullyBlockedDoesNotTriggerRenown() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());
        addCreatureReady(player2, new GiantSpider());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(maulers.isRenowned()).isFalse();
    }
}
