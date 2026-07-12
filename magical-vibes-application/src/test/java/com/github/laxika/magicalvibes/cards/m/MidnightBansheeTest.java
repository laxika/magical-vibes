package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Deathgazer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightBansheeTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("Your upkeep puts a -1/-1 counter on each nonblack creature (all players)")
    void upkeepCountersNonblackCreatures() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(ownBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Black creatures (including the Banshee) get no counter")
    void blackCreaturesUnaffected() {
        Permanent banshee = harness.addToBattlefieldAndReturn(player1, new MidnightBanshee());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new Deathgazer());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(banshee.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(blackCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }
}
