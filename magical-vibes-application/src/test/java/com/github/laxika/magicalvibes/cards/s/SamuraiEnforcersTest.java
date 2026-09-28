package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ValorMadeReal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamuraiEnforcers.class, GiantSpider.class, GrizzlyBears.class, ValorMadeReal.class})
class SamuraiEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("When Samurai Enforcers becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiEnforcers());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);
    }

    @Test
    @DisplayName("When Samurai Enforcers blocks, it gets +2/+2 until end of turn")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent samurai = addCreatureReady(player2, new SamuraiEnforcers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);
    }

    @Test
    @DisplayName("When Samurai Enforcers is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiEnforcers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(4);
    }

    @Test
    @DisplayName("When Samurai Enforcers becomes blocked by multiple creatures, it gets one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiEnforcers());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);
    }

    @Test
    @DisplayName("Samurai Enforcers's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent samurai = addCreatureReady(player1, new SamuraiEnforcers());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(4);
    }

    @Test
    @DisplayName("When Samurai Enforcers blocks multiple creatures, it gets one Bushido bonus")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        Permanent samurai = addCreatureReady(player2, new SamuraiEnforcers());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ValorMadeReal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, samurai.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);
    }
}
