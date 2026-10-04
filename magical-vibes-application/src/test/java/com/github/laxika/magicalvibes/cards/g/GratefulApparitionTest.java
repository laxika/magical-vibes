package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GratefulApparition.class, NarsetParterOfVeils.class})
class GratefulApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player triggers proliferate")
    void proliferatesOnCombatDamageToPlayer() {
        Permanent apparition = addReadyApparition();
        Permanent bears = addCreatureReady(player2, new GratefulApparition());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        apparition.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a planeswalker triggers proliferate")
    void proliferatesOnCombatDamageToPlaneswalker() {
        Permanent apparition = addReadyApparition();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        apparition.setAttackTarget(planeswalker.getId());
        apparition.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.handleMultiplePermanentsChosen(player1, List.of(planeswalker.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger proliferate")
    void doesNotProliferateWhenBlocked() {
        Permanent apparition = addReadyApparition();
        Permanent blocker = addCreatureReady(player2, new GratefulApparition());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent bears = addCreatureReady(player2, new GratefulApparition());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        apparition.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents or players")
    void mayChooseNothing() {
        Permanent apparition = addReadyApparition();
        apparition.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        apparition.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(apparition.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate adds each existing counter kind to chosen permanents and players")
    void proliferatesPermanentsAndPlayersTogether() {
        Permanent apparition = addReadyApparition();
        apparition.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        apparition.setCounterCount(CounterType.SHIELD, 1);
        Permanent unchosen = addCreatureReady(player2, new GratefulApparition());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        apparition.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(apparition.getId(), player2.getId()));

        assertThat(apparition.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(apparition.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    private Permanent addReadyApparition() {
        return addCreatureReady(player1, new GratefulApparition());
    }
}
