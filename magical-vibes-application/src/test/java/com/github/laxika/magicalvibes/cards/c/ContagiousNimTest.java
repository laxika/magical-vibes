package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContagiousNim.class, AlphaTyrranax.class})
class ContagiousNimTest extends BaseCardTest {

    @Test
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new ContagiousNim()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Contagious Nim");
    }

    @Test
    void unblockedDamageAddsPoisonWithoutLifeLoss() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        addCreatureReady(player1, new ContagiousNim());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
    }

    @Test
    void attackingNimDealsCountersToBlockerAndDiesToSimultaneousDamage() {
        addCreatureReady(player1, new ContagiousNim());
        Permanent blocker = addCreatureReady(player2, new AlphaTyrranax());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Contagious Nim");
        harness.assertNotOnBattlefield(player1, "Contagious Nim");
        harness.assertOnBattlefield(player2, "Alpha Tyrranax");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingNimDealsCountersToAttacker() {
        Permanent attacker = addCreatureReady(player1, new AlphaTyrranax());
        addCreatureReady(player2, new ContagiousNim());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Contagious Nim");
        harness.assertNotOnBattlefield(player2, "Contagious Nim");
        harness.assertOnBattlefield(player1, "Alpha Tyrranax");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
