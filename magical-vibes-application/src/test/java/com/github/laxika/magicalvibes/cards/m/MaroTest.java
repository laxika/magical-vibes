package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Maro.class, GrizzlyBears.class})
class MaroTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent maro = addCreatureReady(player1, new Maro());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T updates dynamically as hand size changes")
    void ptUpdatesDynamically() {
        Permanent maro = addCreatureReady(player1, new Maro());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(2);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent maro = addCreatureReady(player1, new Maro());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of Maro's hand-size value")
    void countersApplyOnTopOfHandSize() {
        Permanent maro = addCreatureReady(player1, new Maro());
        maro.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(5);
    }

    @Test
    @DisplayName("Maro dies as a 0/0 when it resolves with an empty hand")
    void diesWithEmptyHandAfterResolving() {
        harness.setHand(player1, List.of(new Maro()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Maro");
        harness.assertInGraveyard(player1, "Maro");
    }

}
