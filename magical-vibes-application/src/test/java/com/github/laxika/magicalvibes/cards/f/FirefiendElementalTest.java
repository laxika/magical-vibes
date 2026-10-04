package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirefiendElemental.class, GiantSpider.class})
class FirefiendElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows attacking while summoning sick and renown follows combat damage")
    void attacksWhileSummoningSick() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new FirefiendElemental());
        elemental.setSummoningSick(true);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elemental.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent elemental = addCreatureReady(player1, new FirefiendElemental());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elemental.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent elemental = addCreatureReady(player1, new FirefiendElemental());
        elemental.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Renown does not trigger when the creature is blocked")
    void noRenownWhenBlocked() {
        Permanent elemental = addCreatureReady(player1, new FirefiendElemental());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(elemental.isRenowned()).isFalse();
    }
}
