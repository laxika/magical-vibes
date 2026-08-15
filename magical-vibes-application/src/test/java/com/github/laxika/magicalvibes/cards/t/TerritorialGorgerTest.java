package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LiveFast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerritorialGorgerTest extends BaseCardTest {

    @Test
    void getsOneBoostForAnEnergyGainEvent() {
        Permanent gorger = addCreatureReady(player1, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(4);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent gorger = addCreatureReady(player1, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        resolveAllTriggers();
        assertThat(gorger.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gorger.getPowerModifier()).isZero();
        assertThat(gorger.getToughnessModifier()).isZero();
    }
}
