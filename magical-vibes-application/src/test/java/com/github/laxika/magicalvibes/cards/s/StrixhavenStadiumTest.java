package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrixhavenStadiumTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds a colorless mana and a point counter")
    void tappingAddsManaAndPointCounter() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to its controller removes a point counter")
    void combatDamageToControllerRemovesPointCounter() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        stadium.setCounterCount(CounterType.POINT, 1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
    }

    @Test
    @DisplayName("Combat damage to an opponent adds a point counter")
    void combatDamageToOpponentAddsPointCounter() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ten point counters make the damaged opponent lose")
    void tenPointCountersMakeDamagedOpponentLose() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        stadium.setCounterCount(CounterType.POINT, 9);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
