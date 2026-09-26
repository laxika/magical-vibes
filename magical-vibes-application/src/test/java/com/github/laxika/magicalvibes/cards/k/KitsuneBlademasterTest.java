package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SireOfTheStorm;
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

@CardUsed({KitsuneBlademaster.class, SireOfTheStorm.class, WanderingOnes.class})
class KitsuneBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("When Kitsune Blademaster becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent blademaster = addCreatureReady(player1, new KitsuneBlademaster());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(1);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Blademaster blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent blademaster = addCreatureReady(player2, new KitsuneBlademaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(1);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Blademaster becomes blocked by multiple creatures, it gets one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent blademaster = addCreatureReady(player1, new KitsuneBlademaster());
        addCreatureReady(player2, new WanderingOnes());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(1);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Blademaster is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent blademaster = addCreatureReady(player1, new KitsuneBlademaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blademaster.getPowerModifier()).isZero();
        assertThat(blademaster.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kitsune Blademaster's first strike lets it survive combat with an equal blocker")
    void firstStrikeDealsCombatDamageBeforeBlocker() {
        Permanent blademaster = addCreatureReady(player1, new KitsuneBlademaster());
        Permanent blocker = addCreatureReady(player2, new SireOfTheStorm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blademaster);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Kitsune Blademaster's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent blademaster = addCreatureReady(player1, new KitsuneBlademaster());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(1);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isZero();
        assertThat(blademaster.getToughnessModifier()).isZero();
    }
}
