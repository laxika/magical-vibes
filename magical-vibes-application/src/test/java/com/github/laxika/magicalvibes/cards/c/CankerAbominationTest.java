package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HelixPinnacle;
import com.github.laxika.magicalvibes.cards.w.WickerboughElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CankerAbomination.class, WickerboughElder.class, HelixPinnacle.class})
class CankerAbominationTest extends BaseCardTest {

    private void castCanker() {
        harness.setHand(player1, List.of(new CankerAbomination()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {2}{B/G}{B/G}
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Enters with a -1/-1 counter for each creature the opponent controls")
    void entersWithCountersFromOpponentCreatures() {
        harness.addToBattlefield(player2, new WickerboughElder());
        harness.addToBattlefield(player2, new WickerboughElder());
        harness.addToBattlefield(player2, new WickerboughElder());

        castCanker();

        Permanent canker = findPermanent(player1, "Canker Abomination");
        assertThat(canker).isNotNull();
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with no counters when the opponent controls no creatures")
    void entersWithNoCountersWhenOpponentHasNoCreatures() {
        castCanker();

        Permanent canker = findPermanent(player1, "Canker Abomination");
        assertThat(canker).isNotNull();
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count creatures the controller controls")
    void doesNotCountOwnCreatures() {
        harness.addToBattlefield(player1, new WickerboughElder());
        harness.addToBattlefield(player1, new WickerboughElder());

        castCanker();

        Permanent canker = findPermanent(player1, "Canker Abomination");
        assertThat(canker).isNotNull();
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dies to SBA when opponent controls enough creatures to zero its toughness")
    void diesWhenEnoughOpponentCreatures() {
        // 6/6 base; six opponent creatures put six -1/-1 counters on it → 0/0
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new WickerboughElder());
        }

        castCanker();

        harness.assertNotOnBattlefield(player1, "Canker Abomination");
    }

    @Test
    @DisplayName("Counts creatures at entry rather than when cast, and counters remain afterward")
    void countsCreaturesAtEntryAndKeepsCounters() {
        harness.setHand(player1, List.of(new CankerAbomination()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.addToBattlefield(player2, new WickerboughElder());

        harness.passBothPriorities();

        Permanent canker = findPermanent(player1, "Canker Abomination");
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player2, new WickerboughElder());
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count noncreature permanents the opponent controls")
    void ignoresOpponentNoncreatures() {
        harness.addToBattlefield(player2, new HelixPinnacle());
        harness.addToBattlefield(player2, new WickerboughElder());

        castCanker();

        Permanent canker = findPermanent(player1, "Canker Abomination");
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the entering creature's controller to determine its opponent")
    void countsOpponentOfSecondPlayer() {
        harness.addToBattlefield(player1, new WickerboughElder());
        harness.addToBattlefield(player1, new WickerboughElder());
        harness.addToBattlefield(player2, new WickerboughElder());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CankerAbomination()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent canker = findPermanent(player2, "Canker Abomination");
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
