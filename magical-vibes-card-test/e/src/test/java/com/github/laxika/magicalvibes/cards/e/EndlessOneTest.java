package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ClutchOfCurrents;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessOne.class, ClutchOfCurrents.class})
class EndlessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new EndlessOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent endlessOne = findPermanent(player1, "Endless One");
        assertThat(endlessOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with no counters at X=0 and dies as a 0/0")
    void entersWithZeroCountersAndDies() {
        harness.setHand(player1, List.of(new EndlessOne()));

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endless One");
        harness.assertInGraveyard(player1, "Endless One");
    }

    @Test
    @DisplayName("Separate casts use their own chosen X values")
    void separateCastsUseTheirOwnXValues() {
        harness.setHand(player1, List.of(new EndlessOne(), new EndlessOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent first = findPermanent(player1, "Endless One");

        harness.castCreature(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Endless One")).hasSize(2);
        Permanent second = findPermanents(player1, "Endless One").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Returning to hand and recasting uses the new X without retaining counters")
    void recastingUsesNewXWithoutRetainingCounters() {
        harness.setHand(player1, List.of(new EndlessOne(), new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 4);
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Endless One");
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.castAndResolveSorcery(player1, 0, original.getId());
        harness.assertNotOnBattlefield(player1, "Endless One");
        harness.assertInHand(player1, "Endless One");

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();

        Permanent recast = findPermanent(player1, "Endless One");
        assertThat(recast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
