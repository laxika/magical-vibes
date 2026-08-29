package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EverflowingChaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without charge counters when not kicked")
    void entersWithoutChargeCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new EverflowingChalice()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent chalice = findChalice();
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Enters with one charge counter per multikicker payment")
    void entersWithChargeCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new EverflowingChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{2}", "{2}"), false);
        harness.passBothPriorities();

        assertThat(findChalice().getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping adds one colorless mana per charge counter")
    void tappingAddsColorlessManaPerChargeCounter() {
        Permanent chalice = harness.addToBattlefieldAndReturn(player1, new EverflowingChalice());
        chalice.setSummoningSick(false);
        chalice.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private Permanent findChalice() {
        return findPermanent(player1, "Everflowing Chalice");
    }
}
