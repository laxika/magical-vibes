package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cystbearer.class, AlphaTyrranax.class})
class CystbearerTest extends BaseCardTest {

    @Test
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new Cystbearer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cystbearer");
    }

    @Test
    void unblockedDamageAddsPoisonWithoutLifeLoss() {
        Permanent attacker = addCreatureReady(player1, new Cystbearer());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void infectDealsCountersWhenAttackingOrBlockingEvenIfCystbearerDies(boolean cystbearerAttacks) {
        Permanent attacker = addCreatureReady(player1,
                cystbearerAttacks ? new Cystbearer() : new AlphaTyrranax());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2,
                cystbearerAttacks ? new AlphaTyrranax() : new Cystbearer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(cystbearerAttacks ? player1 : player2, "Cystbearer");
        harness.assertNotOnBattlefield(cystbearerAttacks ? player1 : player2, "Cystbearer");
        harness.assertOnBattlefield(cystbearerAttacks ? player2 : player1, "Alpha Tyrranax");
        Permanent survivor = cystbearerAttacks ? blocker : attacker;
        assertThat(survivor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(survivor.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
