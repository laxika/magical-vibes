package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HengeWalker.class})
class HengeWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when at least three mana of one color is spent")
    void entersWithCounterWhenThreeManaOfOneColorIsSpent() {
        harness.setHand(player1, List.of(new HengeWalker()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        castAndResolve();

        Permanent walker = findPermanent(player1, "Henge Walker");
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter when mana is split between colors")
    void doesNotEnterWithCounterWhenManaIsSplitBetweenColors() {
        harness.setHand(player1, List.of(new HengeWalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        castAndResolve();

        Permanent walker = findPermanent(player1, "Henge Walker");
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not enter with a counter when three colorless mana is spent")
    void doesNotEnterWithCounterWhenThreeColorlessManaIsSpent() {
        harness.setHand(player1, List.of(new HengeWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castAndResolve();

        Permanent walker = findPermanent(player1, "Henge Walker");
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castAndResolve() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
