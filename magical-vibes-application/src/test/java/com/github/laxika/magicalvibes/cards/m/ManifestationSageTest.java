package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManifestationSageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with +1/+1 counters equal to the cards left in hand")
    void createsFractalWithCountersEqualToHandSize() {
        harness.setHand(player1, List.of(
                new ManifestationSage(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A Fractal with no counters dies as a 0/0")
    void zeroHandFractalDiesToStateBasedActions() {
        harness.setHand(player1, List.of(new ManifestationSage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fractal");
    }
}
