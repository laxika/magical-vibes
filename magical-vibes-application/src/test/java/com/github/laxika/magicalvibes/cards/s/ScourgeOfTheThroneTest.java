package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AureliaTheWarleader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeOfTheThrone.class, AureliaTheWarleader.class, GrizzlyBears.class})
class ScourgeOfTheThroneTest extends BaseCardTest {

    @Test
    void attackingThePlayerWithMostLifeUntapsAttackersAndGrantsExtraCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent scourge = addCreatureReady(player1, new ScourgeOfTheThrone());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        assertThat(bear.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scourge.isTapped()).isFalse();
        assertThat(bear.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenAttackingAPlayerWithLessLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        Permanent scourge = addCreatureReady(player1, new ScourgeOfTheThrone());

        declareAttackers(player1, List.of(0));

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    void firstAttackGateIsConsumedEvenWhenMostLifeConditionIsFalse() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        Permanent scourge = addCreatureReady(player1, new ScourgeOfTheThrone());
        addCreatureReady(player1, new AureliaTheWarleader());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        harness.setLife(player2, 30);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Override
    protected void declareAttackers(Player player, List<Integer> attackerIndices) {
        gd.combatPhasesThisTurn = 1;
        super.declareAttackers(player, attackerIndices);
    }
}
