package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuilledGreatwurmTest extends BaseCardTest {

    @Test
    void putsCombatDamageCountersOnTheCreatureThatDealtTheDamage() {
        Permanent greatwurm = addReady(new Permanent(new QuilledGreatwurm()));
        Permanent attacker = addReady(new Permanent(new GrizzlyBears()));
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(greatwurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureMustSurviveToReceiveTheCounters() {
        addReady(new Permanent(new QuilledGreatwurm()));
        Permanent attacker = addReady(new Permanent(new GrizzlyBears()));
        attacker.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    void doesNotTriggerDuringAnotherPlayersTurn() {
        addReady(new Permanent(new QuilledGreatwurm()));
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castsFromGraveyardByRemovingSixCountersFromControlledCreatures() {
        Permanent creature = addReady(new Permanent(new GrizzlyBears()));
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        QuilledGreatwurm greatwurm = new QuilledGreatwurm();
        harness.setGraveyard(player1, List.of(greatwurm));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyardWithCounterCost(player1, 0,
                List.of(creature.getId(), creature.getId(), creature.getId(),
                        creature.getId(), creature.getId(), creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Quilled Greatwurm");
    }

    @Test
    void rejectsGraveyardCastWithoutEnoughCountersBeforePayingMana() {
        Permanent creature = addReady(new Permanent(new GrizzlyBears()));
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setGraveyard(player1, List.of(new QuilledGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyardWithCounterCost(player1, 0,
                List.of(creature.getId(), creature.getId(), creature.getId(),
                        creature.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Quilled Greatwurm");
    }

    private Permanent addReady(Permanent permanent) {
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
