package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.e.ElementalBond;
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

@CardUsed({SmileAtDeath.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, ElementalBond.class})
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

    @Test
    @DisplayName("You may choose no targets even when eligible creatures are available")
    void mayChooseNoTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("You may return only one of two eligible creatures")
    void mayChooseOneTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, new LlanowarElves()));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Smile at Death does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures in the opponent's graveyard cannot be returned")
    void doesNotReturnOpponentsCards() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A remaining legal target returns when the other target leaves the graveyard")
    void returnsRemainingLegalTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.addToBattlefield(player1, new SmileAtDeath());

        advanceToSmileAtDeathUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.setGraveyard(player1, List.of(elves));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Llanowar Elves")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Elemental Bond does not trigger for a two-power creature returned by Smile at Death")
    void putsCountersAfterEnteringBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefield(player1, new SmileAtDeath());
        harness.addToBattlefield(player1, new ElementalBond());

        advanceToSmileAtDeathUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToSmileAtDeathUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }
}
