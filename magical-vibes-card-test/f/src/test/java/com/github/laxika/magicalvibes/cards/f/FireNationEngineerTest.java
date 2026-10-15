package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.InvasionSubmersible;
import com.github.laxika.magicalvibes.cards.o.OstrichHorse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationEngineer.class, InvasionSubmersible.class, OstrichHorse.class})
class FireNationEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Raid puts a +1/+1 counter on another creature you control at end step")
    void raidPutsCounterOnAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OstrichHorse());
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
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OstrichHorse());
        addEngineer();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Raid only offers another creature or Vehicle you control")
    void targetIsAnotherCreatureOrVehicleYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OstrichHorse());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        Permanent engineer = addEngineer();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new OstrichHorse());
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

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OstrichHorse());
        addEngineer();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTargetItselfWhenNoOtherLegalTargetExists() {
        Permanent engineer = addEngineer();
        harness.addToBattlefield(player2, new OstrichHorse());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerResolvesAfterEngineerLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OstrichHorse());
        Permanent engineer = addEngineer();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(engineer);
        gd.playerGraveyards.get(player1.getId()).add(engineer.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetChangingControllerBeforeResolutionGetsNoCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OstrichHorse());
        addEngineer();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addEngineer() {
        return harness.addToBattlefieldAndReturn(player1, new FireNationEngineer());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
