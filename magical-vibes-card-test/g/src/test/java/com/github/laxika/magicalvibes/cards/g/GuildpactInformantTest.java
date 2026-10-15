package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildpactInformant.class, GrizzlyBears.class, ElspethKnightErrant.class})
class GuildpactInformantTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player triggers proliferate")
    void proliferatesOnCombatDamageToPlayer() {
        Permanent informant = addReadyInformant();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        informant.setAttacking(true);

        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a planeswalker triggers proliferate")
    void proliferatesOnCombatDamageToPlaneswalker() {
        Permanent informant = addReadyInformant();
        Permanent planeswalker = new Permanent(new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        informant.setAttackTarget(planeswalker.getId());
        informant.setAttacking(true);

        resolveCombat();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.handleMultiplePermanentsChosen(player1, List.of(planeswalker.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger proliferate")
    void doesNotProliferateWhenBlocked() {
        Permanent informant = addReadyInformant();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        informant.setAttacking(true);

        resolveCombat();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyInformant() {
        return addCreatureReady(player1, new GuildpactInformant());
    }
}
