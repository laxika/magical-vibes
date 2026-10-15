package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WearAway;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrochiHatchery.class, WearAway.class})
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

    @Test
    @DisplayName("Both X symbols must be paid when casting")
    void requiresTwiceXInMana() {
        harness.setHand(player1, List.of(new OrochiHatchery()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Orochi Hatchery");
        harness.assertInHand(player1, "Orochi Hatchery");
    }

    @Test
    @DisplayName("A newly cast Hatchery can activate its tap ability immediately")
    void canActivateOnTheTurnItEnters() {
        harness.setHand(player1, List.of(new OrochiHatchery()));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        assertThat(findPermanent(player1, "Orochi Hatchery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Token count uses charge counters at resolution")
    void countsCountersAtResolution() {
        Permanent hatchery = addHatcheryReady(player1, 3);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, null, null);

        hatchery.setCounterCount(CounterType.CHARGE, 1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
        assertThat(findPermanents(player2, "Snake")).isEmpty();
    }

    @Test
    @DisplayName("Destroying Hatchery in response does not prevent token creation")
    void createsTokensUsingLastKnownCountersAfterDestruction() {
        Permanent hatchery = addHatcheryReady(player1, 3);
        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, hatchery.getId());
        harness.assertInGraveyard(player1, "Orochi Hatchery");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(3);
        assertThat(findPermanents(player2, "Snake")).isEmpty();
    }

    private Permanent addHatcheryReady(Player player, int chargeCounters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new OrochiHatchery());
        perm.setCounterCount(CounterType.CHARGE, chargeCounters);
        return perm;
    }
}
