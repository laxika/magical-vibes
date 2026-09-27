package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgitatorAnt.class, GrizzlyBears.class})
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

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
