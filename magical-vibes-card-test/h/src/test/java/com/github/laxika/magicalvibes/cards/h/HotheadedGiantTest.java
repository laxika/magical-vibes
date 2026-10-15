package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoubleCleave;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HotheadedGiant.class, HearthfireHobgoblin.class, NettleSentinel.class, DoubleCleave.class})
class HotheadedGiantTest extends BaseCardTest {

    @Test
    @DisplayName("No other red spell cast this turn — enters with two -1/-1 counters")
    void entersWithCountersWhenNoPriorRedSpell() {
        harness.castFromHand(player1, new HotheadedGiant(), "{3}{R}");
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hotheaded Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another red spell cast this turn — enters with no counters")
    void entersWithoutCountersAfterPriorRedSpell() {
        harness.setHand(player1, List.of(new HearthfireHobgoblin(), new HotheadedGiant()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hotheaded Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A prior non-red spell does not prevent the counters")
    void entersWithCountersAfterNonRedSpell() {
        harness.castFromHand(player1, new NettleSentinel(), "{G}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new HotheadedGiant(), "{3}{R}");
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hotheaded Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void anotherGiantCountsAsAnotherRedSpell() {
        harness.castFromHand(player1, new HotheadedGiant(), "{3}{R}");
        harness.passBothPriorities();
        Permanent first = findPermanent(player1, "Hotheaded Giant");

        harness.castFromHand(player1, new HotheadedGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> !p.getId().equals(first.getId()))
                .singleElement()
                .satisfies(p -> assertThat(p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero());
    }

    @Test
    void redInstantCastInResponsePreventsCounters() {
        Permanent sentinel = addCreatureReady(player1, new NettleSentinel());
        harness.castFromHand(player1, new HotheadedGiant(), "{3}{R}");
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, sentinel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hotheaded Giant")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void enteringWithoutBeingCastStillAppliesCounters() {
        Permanent giant = harness.enterBattlefieldAndReturn(player1, new HotheadedGiant());

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's red spell does not prevent the counters")
    void opponentRedSpellDoesNotPreventCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HotheadedGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent sentinel = addCreatureReady(player1, new NettleSentinel());
        harness.setHand(player2, List.of(new DoubleCleave()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, sentinel.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hotheaded Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
