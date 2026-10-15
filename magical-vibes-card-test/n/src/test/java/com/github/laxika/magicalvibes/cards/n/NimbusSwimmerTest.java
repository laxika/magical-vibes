package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimbusSwimmer.class})
class NimbusSwimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=4 enters with 4 +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new NimbusSwimmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 4);

        Permanent swimmer = findPermanent(player1, "Nimbus Swimmer");
        assertThat(swimmer).isNotNull();
        assertThat(swimmer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting with X=0 enters as a 0/0 and dies immediately")
    void entersWithZeroCountersAndDies() {
        harness.setHand(player1, List.of(new NimbusSwimmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Nimbus Swimmer");
        harness.assertInGraveyard(player1, "Nimbus Swimmer");
    }

    @Test
    @DisplayName("Casting with X=1 survives as a 1/1 with one counter")
    void minimumPositiveXSurvives() {
        harness.setHand(player1, List.of(new NimbusSwimmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 1);

        Permanent swimmer = findPermanent(player1, "Nimbus Swimmer");
        assertThat(swimmer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, swimmer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swimmer)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Nimbus Swimmer");
    }
}
