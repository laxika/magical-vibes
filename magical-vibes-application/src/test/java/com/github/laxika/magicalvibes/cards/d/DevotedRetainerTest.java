package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevotedRetainer.class, HumbleBudoka.class})
class DevotedRetainerTest extends BaseCardTest {

    @Test
    @DisplayName("When Devoted Retainer becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        retainer.setAttacking(true);
        addCreatureReady(player2, new HumbleBudoka());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Devoted Retainer blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new HumbleBudoka());
        attacker.setAttacking(true);
        Permanent retainer = addCreatureReady(player2, new DevotedRetainer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Devoted Retainer becomes blocked by multiple creatures, it gets only one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        retainer.setAttacking(true);
        addCreatureReady(player2, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Devoted Retainer is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        retainer.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(retainer.getPowerModifier()).isZero();
        assertThat(retainer.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Devoted Retainer's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        retainer.setAttacking(true);
        addCreatureReady(player2, new HumbleBudoka());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isZero();
        assertThat(retainer.getToughnessModifier()).isZero();
    }
}
