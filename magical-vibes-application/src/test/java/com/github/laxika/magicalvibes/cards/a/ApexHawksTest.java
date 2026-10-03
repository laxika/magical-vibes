package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApexHawks.class})
class ApexHawksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without +1/+1 counters when not multikicked")
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hawks = findHawks();
        assertThat(hawks).isNotNull();
        assertThat(hawks.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter per multikicker payment")
    void entersWithCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{W}", "{1}{W}"));
        harness.passBothPriorities();

        Permanent hawks = findHawks();
        assertThat(hawks).isNotNull();
        assertThat(hawks.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A single multikicker payment adds one counter on entry")
    void entersWithOneCounterWhenKickedOnce() {
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{W}"));
        harness.passBothPriorities();

        assertThat(findHawks()).isNotNull();
        assertThat(findHawks().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multikicker cannot be paid without enough additional mana")
    void cannotKickWithOnlyBaseCostMana() {
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{1}{W}")))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Apex Hawks");
        harness.assertNotOnBattlefield(player1, "Apex Hawks");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast gives no multikicker counters")
    void entersWithoutCountersWhenNotCast() {
        Permanent hawks = harness.enterBattlefieldAndReturn(player1, new ApexHawks());

        assertThat(hawks.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findHawks() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Apex Hawks"))
                .findFirst()
                .orElse(null);
    }
}
