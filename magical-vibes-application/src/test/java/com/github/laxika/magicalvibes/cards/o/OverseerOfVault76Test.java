package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BloatflySwarm;
import com.github.laxika.magicalvibes.cards.m.MarshalsAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverseerOfVault76.class, AirElemental.class, GrizzlyBears.class, BloatflySwarm.class, MarshalsAnthem.class})
class OverseerOfVault76Test extends BaseCardTest {

    @Test
    void putsQuestCountersOnItselfAndSmallCreatures() {
        Permanent overseer = harness.enterBattlefieldAndReturn(player1, new OverseerOfVault76());
        harness.passBothPriorities();
        assertThat(overseer.getCounterCount(CounterType.QUEST)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(overseer.getCounterCount(CounterType.QUEST)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new AirElemental());
        harness.passBothPriorities();
        assertThat(overseer.getCounterCount(CounterType.QUEST)).isEqualTo(2);
    }

    @Test
    void acceptingCombatAbilityBoostsOwnCreaturesAndRemovesQuestCounters() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        overseer.setCounterCount(CounterType.QUEST, 3);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(overseer.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isTrue();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void decliningCombatAbilityLeavesQuestCountersUnchanged() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        overseer.setCounterCount(CounterType.QUEST, 3);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(overseer.getCounterCount(CounterType.QUEST)).isEqualTo(3);
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canPayFromSeveralControlledPermanents() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        overseer.setCounterCount(CounterType.QUEST, 1);
        ownBear.setCounterCount(CounterType.QUEST, 2);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleListChoice(player1, "Overseer of Vault 76");
        resolveAllTriggers();

        assertThat(overseer.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(ownBear.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringCountersCountTowardPowerThreshold() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());

        Permanent swarm = harness.enterBattlefieldAndReturn(player1, new BloatflySwarm());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(5);
        assertThat(overseer.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void continuousBoostsCountTowardPowerThreshold() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        harness.addToBattlefield(player1, new MarshalsAnthem());
        harness.addToBattlefield(player1, new MarshalsAnthem());

        Permanent bear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(overseer.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void opposingCreatureDoesNotAddQuestCounter() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(overseer.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void payingCreatesSeparateTriggerBeforeRewardResolves() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        overseer.setCounterCount(CounterType.QUEST, 3);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(overseer.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.VIGILANCE)).isTrue();
    }
    @Test
    void rewardOnlyAffectsCreaturesPresentAndVigilanceExpires() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        overseer.setCounterCount(CounterType.QUEST, 4);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(overseer.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.VIGILANCE)).isTrue();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void combatAbilityDoesNotTriggerOnOpponentsTurn() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new OverseerOfVault76());
        overseer.setCounterCount(CounterType.QUEST, 3);

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(overseer.getCounterCount(CounterType.QUEST)).isEqualTo(3);
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
