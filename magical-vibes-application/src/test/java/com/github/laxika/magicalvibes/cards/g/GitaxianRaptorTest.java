package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitaxianRaptorTest extends BaseCardTest {

    @Test
    void entersWithThreeOilCounters() {
        harness.setHand(player1, List.of(new GitaxianRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Gitaxian Raptor");
        assertThat(raptor.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void removesOilCounterAndBoostsSelfUntilEndOfTurn() {
        Permanent raptor = addReadyRaptor(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raptor.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(raptor.getEffectivePower()).isEqualTo(2);
        assertThat(raptor.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raptor.getEffectivePower()).isEqualTo(1);
        assertThat(raptor.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void cannotActivateWithoutOilCounters() {
        Permanent raptor = addReadyRaptor(player1);
        raptor.setCounterCount(CounterType.OIL, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRaptor(Player player) {
        Permanent raptor = addCreatureReady(player, new GitaxianRaptor());
        raptor.setCounterCount(CounterType.OIL, 3);
        return raptor;
    }
}
