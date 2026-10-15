package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RushwoodElemental.class)
class RushwoodElementalTest extends BaseCardTest {

    @Test
    void acceptingUpkeepTriggerPutsCounterOnItself() {
        Permanent elemental = addCreatureReady(player1, new RushwoodElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningUpkeepTriggerDoesNotPutCounterOnItself() {
        Permanent elemental = addCreatureReady(player1, new RushwoodElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent elemental = addCreatureReady(player1, new RushwoodElemental());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countersAccumulateAcrossControllersUpkeeps() {
        Permanent elemental = addCreatureReady(player1, new RushwoodElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void otherPlayersElementalGetsCounterOnlyOnItsControllersUpkeep() {
        Permanent ownElemental = addCreatureReady(player1, new RushwoodElemental());
        Permanent opposingElemental = addCreatureReady(player2, new RushwoodElemental());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(opposingElemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownElemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
