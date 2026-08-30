package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BoostedSloop;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationEngineer.class, BoostedSloop.class, GrizzlyBears.class})
class FireNationEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Raid puts a +1/+1 counter on another creature you control at end step")
    void raidPutsCounterOnAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent engineer = addEngineer();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Raid can target a Vehicle you control")
    void raidPutsCounterOnVehicleYouControl() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        addEngineer();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep();
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Raid does not trigger when you did not attack this turn")
    void raidDoesNotTriggerWithoutAnAttack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addEngineer();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Raid only offers another creature or Vehicle you control")
    void targetIsAnotherCreatureOrVehicleYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        Permanent engineer = addEngineer();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId(), vehicle.getId());
        assertThat(choice.validIds()).doesNotContain(engineer.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addEngineer() {
        return harness.addToBattlefieldAndReturn(player1, new FireNationEngineer());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
