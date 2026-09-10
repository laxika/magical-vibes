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
