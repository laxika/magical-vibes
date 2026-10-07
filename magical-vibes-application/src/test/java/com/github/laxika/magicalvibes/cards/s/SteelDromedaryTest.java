package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelDromedary.class, OrnithopterOfParadise.class})
class SteelDromedaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with two +1/+1 counters")
    void entersTappedWithTwoCounters() {
        Permanent dromedary = castDromedary();

        assertThat(dromedary.isTapped()).isTrue();
        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doesn't untap while it has a +1/+1 counter")
    void doesNotUntapWithCounter() {
        Permanent dromedary = castDromedary();
        dromedary.tap();

        harness.performUntapStep(player1);

        assertThat(dromedary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Moves a +1/+1 counter to another creature at the beginning of combat")
    void movesCounterAtBeginningOfCombat() {
        Permanent dromedary = castDromedary();
        Permanent bears = addCreatureReady(player1, new OrnithopterOfParadise());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May decline moving a counter")
    void mayDeclineMovingCounter() {
        Permanent dromedary = castDromedary();
        Permanent bears = addCreatureReady(player1, new OrnithopterOfParadise());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target itself, but moving a counter onto itself changes nothing")
    void canTargetItselfWithoutMovingCounter() {
        Permanent dromedary = castDromedary();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, dromedary.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can move a counter onto an opponent's creature")
    void movesCounterOntoOpponentsCreature() {
        Permanent dromedary = castDromedary();
        Permanent target = addCreatureReady(player2, new OrnithopterOfParadise());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps during its controller's untap step after its last counter is removed")
    void untapsWithoutCounters() {
        Permanent dromedary = castDromedary();
        dromedary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.performUntapStep(player1);

        assertThat(dromedary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot place a counter when the source has no counter left")
    void doesNotMoveCounterWithoutSourceCounter() {
        Permanent dromedary = castDromedary();
        Permanent target = addCreatureReady(player1, new OrnithopterOfParadise());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        dromedary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger at the beginning of the opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent dromedary = castDromedary();
        Permanent target = addCreatureReady(player2, new OrnithopterOfParadise());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castDromedary() {
        harness.castFromHand(player1, new SteelDromedary(), "{3}");
        harness.passBothPriorities();
        return findPermanent(player1, "Steel Dromedary");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
