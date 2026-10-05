package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirriTheCursed.class, AvenRiftwatcher.class})
class MirriTheCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when it deals combat damage to a creature")
    void getsCounterWhenDealingCombatDamageToCreature() {
        Permanent mirri = addCreatureReady(player1, new MirriTheCursed());
        addCreatureReady(player2, new AvenRiftwatcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when it deals combat damage to a player")
    void doesNotGetCounterWhenDealingCombatDamageToPlayer() {
        Permanent mirri = addCreatureReady(player1, new MirriTheCursed());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets a counter when blocking and killing an attacker with first strike")
    void getsCounterWhenBlocking() {
        addCreatureReady(player1, new AvenRiftwatcher());
        Permanent mirri = addCreatureReady(player2, new MirriTheCursed());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mirri);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AvenRiftwatcher);
    }

    @Test
    @DisplayName("Can attack immediately with haste without triggering its counter ability against a player")
    void attacksWhileSummoningSick() {
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriTheCursed());
        mirri.setSummoningSick(true);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
