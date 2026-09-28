package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NezumiRonin.class, HumbleBudoka.class})
class NezumiRoninTest extends BaseCardTest {

    @Test
    @DisplayName("When Nezumi Ronin becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new NezumiRonin());
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Nezumi Ronin blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new HumbleBudoka());
        Permanent ronin = addCreatureReady(player2, new NezumiRonin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Nezumi Ronin is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new NezumiRonin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Nezumi Ronin becomes blocked by multiple creatures, it gets only one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new NezumiRonin());
        addCreatureReady(player2, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Nezumi Ronin blocks multiple creatures, it gets only one Bushido bonus")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player1, new HumbleBudoka());
        NezumiRonin card = new NezumiRonin();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent ronin = addCreatureReady(player2, card);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Nezumi Ronin's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent ronin = addCreatureReady(player1, new NezumiRonin());
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }
}
