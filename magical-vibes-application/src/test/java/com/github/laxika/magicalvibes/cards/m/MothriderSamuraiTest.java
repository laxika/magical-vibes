package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SoratamiCloudskater;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MothriderSamurai.class, SoratamiCloudskater.class, WanderingOnes.class})
class MothriderSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("When Mothrider Samurai becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player2, new SoratamiCloudskater());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("When Mothrider Samurai blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent mothrider = addCreatureReady(player2, new MothriderSamurai());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("When Mothrider Samurai is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Mothrider Samurai becomes blocked by multiple creatures, it gets one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player2, new SoratamiCloudskater());
        addCreatureReady(player2, new SoratamiCloudskater());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mothrider Samurai's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player2, new SoratamiCloudskater());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(2);
    }
}
