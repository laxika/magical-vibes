package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bulette.class, BurningHands.class})
class BuletteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at its controller's end step when a creature died this turn")
    void getsCounterWhenCreatureDied() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when no creature died this turn")
    void doesNotGetCounterWithoutCreatureDeath() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());

        advanceToEndStep(player1);

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player2);

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An actual opposing creature death enables the end-step counter")
    void getsCounterAfterOpposingCreatureDies() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Bulette());

        burnCreature(victim);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(victim.getCard());
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An actual friendly creature death enables the end-step counter")
    void getsCounterAfterFriendlyCreatureDies() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new Bulette());

        burnCreature(victim);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(victim.getCard());
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple creature deaths still grant only one counter")
    void multipleDeathsGrantOnlyOneCounter() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        Permanent firstVictim = harness.addToBattlefieldAndReturn(player2, new Bulette());
        Permanent secondVictim = harness.addToBattlefieldAndReturn(player2, new Bulette());

        burnCreature(firstVictim);
        burnCreature(secondVictim);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not cause a late trigger")
    void deathDuringEndStepDoesNotTrigger() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Bulette());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        burnCreature(victim);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A death before Bulette enters still enables its end-step trigger")
    void deathBeforeEnteringEnablesTrigger() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Bulette());
        burnCreature(victim);
        Permanent bulette = harness.enterBattlefieldAndReturn(player1, new Bulette());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void burnCreature(Permanent victim) {
        harness.setHand(player1, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
