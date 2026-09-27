package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelDromedary.class, GrizzlyBears.class})
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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dromedary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target itself with the beginning-of-combat ability")
    void cannotTargetItself() {
        Permanent dromedary = castDromedary();

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, dromedary.getId()))
                .isInstanceOf(IllegalStateException.class);
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
