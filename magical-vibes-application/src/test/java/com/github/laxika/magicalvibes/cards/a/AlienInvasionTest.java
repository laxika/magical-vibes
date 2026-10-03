package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlienInvasion.class, DoublingSeason.class})
class AlienInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an hasty Alien with one counter for each invasion counter")
    void createsAlienWithInvasionCountPlusOneCounters() {
        Permanent invasion = harness.addToBattlefieldAndReturn(player1, new AlienInvasion());
        invasion.setCounterCount(CounterType.INVASION, 2);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent alien = findPermanent(player1, "Alien");
        assertThat(alien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(alien.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(invasion.getCounterCount(CounterType.INVASION)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Alien must attack each combat if able")
    void alienMustAttack() {
        harness.addToBattlefieldAndReturn(player1, new AlienInvasion());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(1);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, java.util.List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Does not trigger on an opponent's beginning of combat")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent invasion = harness.addToBattlefieldAndReturn(player1, new AlienInvasion());

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(invasion.getCounterCount(CounterType.INVASION)).isZero();
        assertThat(findPermanents(player1, "Alien")).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("The first Alien has no counters and later combats use the accumulated invasion counters")
    void successiveCombatsGrowOnlyTheNewAlien() {
        Permanent invasion = harness.addToBattlefieldAndReturn(player1, new AlienInvasion());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent firstAlien = findPermanent(player1, "Alien");
        assertThat(firstAlien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(invasion.getCounterCount(CounterType.INVASION)).isEqualTo(1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(2);
        assertThat(firstAlien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Alien").stream()
                .filter(permanent -> !permanent.getId().equals(firstAlien.getId())))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
        assertThat(invasion.getCounterCount(CounterType.INVASION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Every Alien created by Doubling Season receives the invasion-based counters")
    void doubledTokensEachReceiveCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent invasion = harness.addToBattlefieldAndReturn(player1, new AlienInvasion());
        invasion.setCounterCount(CounterType.INVASION, 2);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(2)
                .allSatisfy(alien -> assertThat(alien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(4));
        assertThat(invasion.getCounterCount(CounterType.INVASION)).isEqualTo(4);
    }
}
