package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NyleasDisciple;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Reverent Hunter")
@CardUsed({ReverentHunter.class, NyleasDisciple.class})
class ReverentHunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts counters on itself equal to green devotion, including itself")
    void etbPutsCountersEqualToGreenDevotion() {
        harness.addToBattlefield(player1, new NyleasDisciple());
        harness.setHand(player1, List.of(new ReverentHunter()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        var hunter = findPermanent(player1, "Reverent Hunter");
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only controlled permanents contribute to devotion")
    void ignoresOpponentsPermanentsAndCardsInHand() {
        harness.addToBattlefield(player2, new NyleasDisciple());
        harness.setHand(player1, List.of(new ReverentHunter(), new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        var hunter = findPermanent(player1, "Reverent Hunter");
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Devotion is evaluated when the entering trigger resolves")
    void countsPermanentsAddedBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new ReverentHunter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var hunter = findPermanent(player1, "Reverent Hunter");
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new NyleasDisciple());
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
