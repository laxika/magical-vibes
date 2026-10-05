package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManifestationSage.class, Forest.class})
class ManifestationSageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with +1/+1 counters equal to the cards left in hand")
    void createsFractalWithCountersEqualToHandSize() {
        harness.setHand(player1, List.of(
                new ManifestationSage(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Counts the controller's current hand when the entry trigger resolves")
    void countsHandAtTriggerResolution() {
        harness.setHand(player1, List.of(new ManifestationSage(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        resolveAllTriggers();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Fractal"))).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Fractal");

        harness.setHand(player1, List.of());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A hand emptied before the entry trigger resolves produces no surviving Fractal")
    void handEmptiedBeforeTriggerResolution() {
        harness.setHand(player1, List.of(new ManifestationSage(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Manifestation Sage");
        harness.assertNotOnBattlefield(player1, "Fractal");
    }
}
