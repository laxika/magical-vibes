package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({IchorSynthesizer.class, Shock.class, GrizzlyBears.class})
class IchorSynthesizerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Ichor Synthesizer")
    void noncreatureSpellPutsOilCounter() {
        Permanent synthesizer = addSynthesizerReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Ichor Synthesizer")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent synthesizer = addSynthesizerReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Four oil counters give Ichor Synthesizer +2/+0 and make it unblockable")
    void fourOilCountersGrantPowerAndUnblockable() {
        Permanent synthesizer = addSynthesizerReady(player1);
        synthesizer.setCounterCount(CounterType.OIL, 3);

        assertThat(gqs.getEffectivePower(gd, synthesizer)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, synthesizer)).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, synthesizer)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, synthesizer)).isTrue();
    }

    @Test
    @DisplayName("The oil counter trigger resolves before the noncreature spell")
    void counterArrivesBeforeSpellResolves() {
        Permanent synthesizer = addSynthesizerReady(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isZero();
        harness.passBothPriorities();

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.assertLife(player2, 20);

        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not add an oil counter")
    void opponentSpellDoesNotAddCounter() {
        Permanent synthesizer = addSynthesizerReady(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Each controlled Synthesizer gets its own oil counter")
    void multipleSynthesizersTriggerIndependently() {
        Permanent first = addSynthesizerReady(player1);
        Permanent second = addSynthesizerReady(player1);
        Permanent opposing = addSynthesizerReady(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("The bonus applies above four counters and disappears below four")
    void thresholdTracksCurrentOilCounters() {
        Permanent synthesizer = addSynthesizerReady(player1);
        synthesizer.setCounterCount(CounterType.OIL, 5);

        assertThat(gqs.getEffectivePower(gd, synthesizer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, synthesizer)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, synthesizer)).isTrue();

        synthesizer.setCounterCount(CounterType.OIL, 3);

        assertThat(gqs.getEffectivePower(gd, synthesizer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, synthesizer)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, synthesizer)).isFalse();
    }

    private Permanent addSynthesizerReady(Player player) {
        return addCreatureReady(player, new IchorSynthesizer());
    }
}
