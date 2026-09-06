package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiratePeddlers.class, ZuranOrb.class, Forest.class})
class PiratePeddlersTest extends BaseCardTest {

    @Test
    @DisplayName("Putting another permanent into a graveyard by sacrificing it adds a +1/+1 counter")
    void gainsCounterWhenAnotherPermanentIsSacrificed() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        prepareMainPhase(player1);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent sacrificing a permanent does not add a counter")
    void doesNotTriggerForOpponentSacrifice() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());
        prepareMainPhase(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Forest");
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
