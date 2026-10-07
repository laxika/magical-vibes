package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruStalwart.class})
class TajuruStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless mana spent does not count as a color for converge")
    void colorlessManaDoesNotAddCounters() {
        harness.castFromHand(player1, new TajuruStalwart(), "{2}{G}");
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Tajuru Stalwart");
        assertThat(stalwart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering without being cast gives no converge counters")
    void enteringWithoutCastingGivesNoCounters() {
        Permanent stalwart = harness.enterBattlefieldAndReturn(player1, new TajuruStalwart());

        assertThat(stalwart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stalwart);
    }

    @Test
    @DisplayName("Repeated mana of a color counts only once alongside a second color")
    void repeatedColorCountsOnlyOnce() {
        harness.setHand(player1, List.of(new TajuruStalwart()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Tajuru Stalwart");
        assertThat(stalwart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter when one color of mana is spent")
    void entersWithOneCounterForOneColor() {
        harness.setHand(player1, List.of(new TajuruStalwart()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Tajuru Stalwart");
        assertThat(stalwart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with one counter for each color of mana spent")
    void entersWithCountersForDistinctColors() {
        harness.setHand(player1, List.of(new TajuruStalwart()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Tajuru Stalwart");
        assertThat(stalwart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
