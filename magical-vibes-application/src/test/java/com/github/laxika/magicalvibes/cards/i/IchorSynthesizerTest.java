package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IchorSynthesizerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Ichor Synthesizer")
    void noncreatureSpellPutsOilCounter() {
        Permanent synthesizer = addSynthesizerReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(synthesizer.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, synthesizer)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, synthesizer)).isTrue();
    }

    private Permanent addSynthesizerReady(Player player) {
        return addCreatureReady(player, new IchorSynthesizer());
    }
}
