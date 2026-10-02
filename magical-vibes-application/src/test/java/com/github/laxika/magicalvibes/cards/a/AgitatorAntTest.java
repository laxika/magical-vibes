package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgitatorAnt.class, GrizzlyBears.class, DeadlyInsect.class})
class AgitatorAntTest extends BaseCardTest {

    @Test
    void eachPlayerMayBoostAndGoadTheirChosenCreature() {
        harness.addToBattlefield(player1, new AgitatorAnt());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1Creature.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player2Creature.getId());
        resolveAgitatorAbilities();

        assertThat(player1Creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(player2Creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, player1Creature)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, player2Creature)).isEqualTo(1);
    }

    @Test
    void goadExpiresAtAgitatorControllersNextTurn() {
        harness.addToBattlefield(player1, new AgitatorAnt());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1Creature.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player2Creature.getId());
        resolveAgitatorAbilities();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(als.getMustAttackRequirementCount(gd, player1Creature)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, player2Creature)).isZero();
    }

    @Test
    void countersAndGoadAreAppliedWithoutAdditionalStackResolutions() {
        Permanent ant = harness.addToBattlefieldAndReturn(player1, new AgitatorAnt());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ant.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, opponentCreature.getId());

        assertThat(ant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, ant)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, opponentCreature)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureWithShroudCanBeChosenBecauseTheAbilityDoesNotTarget() {
        harness.addToBattlefield(player1, new AgitatorAnt());
        Permanent insect = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(insect.getId());
        harness.handlePermanentChosen(player1, insect.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAgitatorAbilities();

        assertThat(insect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, insect)).isEqualTo(1);
    }

    @Test
    void decliningDoesNotPutCountersOnOrGoadAnyCreature() {
        Permanent ant = harness.addToBattlefieldAndReturn(player1, new AgitatorAnt());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(ant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, ant)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, opponentCreature)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent ant = harness.addToBattlefieldAndReturn(player1, new AgitatorAnt());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    private void resolveAgitatorAbilities() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
