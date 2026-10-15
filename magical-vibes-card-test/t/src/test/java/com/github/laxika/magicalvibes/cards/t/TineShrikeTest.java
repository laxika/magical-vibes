package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BrassSquire;
import com.github.laxika.magicalvibes.cards.m.MirranSpy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TineShrike.class, BrassSquire.class, MirranSpy.class})
class TineShrikeTest extends BaseCardTest {

    @Test
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new TineShrike()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Tine Shrike");
    }

    @Test
    void unblockedDamageGivesPoisonInsteadOfLifeLoss() {
        addCreatureReady(player1, new TineShrike());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new TineShrike());
        addCreatureReady(player2, new BrassSquire());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingBlockerReceivesCountersInsteadOfMarkedDamage() {
        addCreatureReady(player1, new TineShrike());
        Permanent blocker = addCreatureReady(player2, new MirranSpy());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertInGraveyard(player1, "Tine Shrike");
        harness.assertOnBattlefield(player2, "Mirran Spy");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingGroundCreatureDealsInfectDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new BrassSquire());
        addCreatureReady(player2, new TineShrike());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertInGraveyard(player2, "Tine Shrike");
        harness.assertOnBattlefield(player1, "Brass Squire");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void poisonAddsToExistingCountersAndCausesLossAtTen() {
        addCreatureReady(player1, new TineShrike());
        gd.playerPoisonCounters.put(player2.getId(), 8);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
