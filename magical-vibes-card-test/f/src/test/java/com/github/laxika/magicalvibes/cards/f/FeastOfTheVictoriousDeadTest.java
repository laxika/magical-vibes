package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeastOfTheVictoriousDead.class, GrizzlyBears.class})
class FeastOfTheVictoriousDeadTest extends BaseCardTest {

    @Test
    void gainsLifeForAllCreatureDeathsAndDistributesCountersAmongControlledCreatures() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.XValueChoice finalChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(finalChoice).isNotNull();
        assertThat(finalChoice.minValue()).isEqualTo(1);
        assertThat(finalChoice.maxValue()).isEqualTo(1);
        harness.handleXValueChosen(player1, 1);

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenNoCreatureDiedThisTurn() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsCreaturesThatDieInResponseToTheTrigger() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstDeath = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent laterDeath = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, firstDeath);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, laterDeath);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void gainsLifeEvenWhenNoControlledCreaturesRemain() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, creature);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canSkipOneCreatureAndPutAllCountersOnAnother() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent skipped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstDeath = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondDeath = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, firstDeath);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, secondDeath);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player1, 2);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(skipped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, creature);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathAfterEndStepBeginsDoesNotCreateATrigger() {
        harness.addToBattlefield(player1, new FeastOfTheVictoriousDead());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToEndStep(player1);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, creature);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
