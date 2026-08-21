package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmileAtDeath.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class SmileAtDeathTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, returns up to two targeted small creatures and puts counters on them")
    void returnsTwoSmallCreaturesWithCounters() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        GiantGrowth noncreature = new GiantGrowth();
        harness.setGraveyard(player1, List.of(bears, elves, new HillGiant(), noncreature));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent returnedElves = findPermanent(player1, "Llanowar Elves");
        assertThat(returnedElves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    @DisplayName("A creature with power greater than two is not a legal target")
    void doesNotReturnCreatureAbovePowerLimit() {
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Hill Giant")).isZero();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private void advanceToSmileAtDeathUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
