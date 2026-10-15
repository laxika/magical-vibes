package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuilledGreatwurm.class, BearCub.class, WitnessProtection.class})
class QuilledGreatwurmTest extends BaseCardTest {

    @Test
    void putsCombatDamageCountersOnTheCreatureThatDealtTheDamage() {
        Permanent greatwurm = addCreatureReady(player1, new QuilledGreatwurm());
        Permanent attacker = addCreatureReady(player1, new BearCub());
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
        addCreatureReady(player1, new QuilledGreatwurm());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BearCub());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(attacker.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Bear Cub");
    }

    @Test
    void doesNotTriggerDuringAnotherPlayersTurn() {
        Permanent blocker = addCreatureReady(player1, new QuilledGreatwurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new BearCub());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castsFromGraveyardByRemovingSixCountersFromControlledCreatures() {
        Permanent creature = addCreatureReady(player1, new BearCub());
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
        Permanent creature = addCreatureReady(player1, new BearCub());
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

    @Test
    void canPayWithCountersOtherThanPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        creature.setCounterCount(CounterType.STUN, 6);
        harness.setGraveyard(player1, List.of(new QuilledGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyardWithCounterCost(player1, 0,
                List.of(creature.getId(), creature.getId(), creature.getId(),
                        creature.getId(), creature.getId(), creature.getId()));

        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Quilled Greatwurm");
    }

    @Test
    void canSplitMixedCounterPaymentAmongCreatures() {
        Permanent first = addCreatureReady(player1, new BearCub());
        Permanent second = addCreatureReady(player1, new BearCub());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.STUN, 3);
        harness.setGraveyard(player1, List.of(new QuilledGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyardWithCounterCost(player1, 0,
                List.of(first.getId(), first.getId(), first.getId(),
                        second.getId(), second.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.STUN)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Quilled Greatwurm");
    }

    @Test
    void cannotCastFromGraveyardDuringCombat() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        harness.setGraveyard(player1, List.of(new QuilledGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castFromGraveyardWithCounterCost(player1, 0,
                List.of(creature.getId(), creature.getId(), creature.getId(),
                        creature.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Quilled Greatwurm");
    }

    @Test
    void greatwurmReceivesCountersForItsOwnCombatDamage() {
        Permanent greatwurm = addCreatureReady(player1, new QuilledGreatwurm());
        greatwurm.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(greatwurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void countersStillTriggerForCreatureThatHasLostAllAbilities() {
        addCreatureReady(player1, new QuilledGreatwurm());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void survivingCreatureReceivesCountersWhenGreatwurmDiesInSameCombat() {
        Permanent greatwurm = addCreatureReady(player1, new QuilledGreatwurm());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        greatwurm.setAttacking(true);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new QuilledGreatwurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quilled Greatwurm");
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

}
