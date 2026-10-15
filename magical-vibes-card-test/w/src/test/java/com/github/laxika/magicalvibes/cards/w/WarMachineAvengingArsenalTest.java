package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarMachineAvengingArsenal.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class})
class WarMachineAvengingArsenalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives double strike to attacking modified creatures you control")
    void attackingGivesDoubleStrikeToAttackingModifiedCreatures() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        warMachine.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warMachine, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger's double strike grant wears off at end of turn")
    void doubleStrikeGrantWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void equipmentAndOwnAurasModifyButOpponentsAurasDoNot() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedByOpponent = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(equipped.getId());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        ownAura.setAttachedTo(enchanted.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        opposingAura.setAttachedTo(enchantedByOpponent.getId());

        declareAttackers(player1, List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warMachine, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantedByOpponent, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void modificationIsCheckedWhenTheTriggerResolves() {
        addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent gainsCounter = addCreatureReady(player1, new GrizzlyBears());
        Permanent losesCounter = addCreatureReady(player1, new GrizzlyBears());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(gd.stack).isNotEmpty();
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void grantPersistsAfterLosingModificationAndDoesNotIncludeLaterModifiedCreatures() {
        addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        unmodified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modified, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void warMachineMustAttackForItsAbilityToTrigger() {
        addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void nonStatCountersModifyAttackersButModifiedNonAttackersAreExcluded() {
        addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        nonAttacker.setCounterCount(CounterType.CHARGE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
