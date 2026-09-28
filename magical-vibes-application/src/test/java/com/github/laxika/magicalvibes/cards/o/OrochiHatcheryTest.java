package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OrochiHatchery.class)
class OrochiHatcheryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 charge counters")
    void entersWithXChargeCounters() {
        harness.setHand(player1, List.of(new OrochiHatchery()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Orochi Hatchery").getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters with no charge counters")
    void entersWithNoCountersForXZero() {
        harness.setHand(player1, List.of(new OrochiHatchery()));

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Orochi Hatchery").getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Ability creates one 1/1 Snake per charge counter")
    void createsOneSnakePerChargeCounter() {
        Permanent hatchery = addHatcheryReady(player1, 3);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> snakes = findPermanents(player1, "Snake");
        assertThat(snakes).hasSize(3);
        assertThat(snakes).allSatisfy(snake -> {
            assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(1);
        });
        assertThat(hatchery.isTapped()).isTrue();
        assertThat(hatchery.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability with no charge counters creates no tokens")
    void createsNoTokensWithoutCounters() {
        addHatcheryReady(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).isEmpty();
    }

    private Permanent addHatcheryReady(Player player, int chargeCounters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new OrochiHatchery());
        perm.setCounterCount(CounterType.CHARGE, chargeCounters);
        return perm;
    }
}
