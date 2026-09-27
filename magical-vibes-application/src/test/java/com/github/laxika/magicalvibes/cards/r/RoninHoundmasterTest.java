package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoninHoundmaster.class, WanderingOnes.class})
class RoninHoundmasterTest extends BaseCardTest {

    @Test
    @DisplayName("When Ronin Houndmaster becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent houndmaster = addReadyHoundmaster(player1);
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Ronin Houndmaster blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent houndmaster = addReadyHoundmaster(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Ronin Houndmaster is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent houndmaster = addReadyHoundmaster(player1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(houndmaster.getPowerModifier()).isZero();
        assertThat(houndmaster.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Ronin Houndmaster becomes blocked by multiple creatures, it gets only one bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent houndmaster = addReadyHoundmaster(player1);
        addCreatureReady(player2, new WanderingOnes());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Ronin Houndmaster blocks multiple creatures, it gets only one bonus")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        addCreatureReady(player1, new WanderingOnes());
        RoninHoundmaster card = new RoninHoundmaster();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent houndmaster = addCreatureReady(player2, card);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ronin Houndmaster's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent houndmaster = addReadyHoundmaster(player1);
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(houndmaster.getPowerModifier()).isZero();
        assertThat(houndmaster.getToughnessModifier()).isZero();
    }

    private Permanent addReadyHoundmaster(Player player) {
        return addCreatureReady(player, new RoninHoundmaster());
    }
}
