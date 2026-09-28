package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoninCavekeeper.class, GodosIrregulars.class})
class RoninCavekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("When Ronin Cavekeeper becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent ronin = addReadyRonin(player1);
        ronin.setAttacking(true);
        addReadyGodosIrregulars(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(2);
        assertThat(ronin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Ronin Cavekeeper blocks, it gets +2/+2 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyGodosIrregulars(player1);
        attacker.setAttacking(true);
        Permanent ronin = addReadyRonin(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(2);
        assertThat(ronin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Ronin Cavekeeper is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent ronin = addReadyRonin(player1);
        ronin.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent ronin = addReadyRonin(player1);
        ronin.setAttacking(true);
        addReadyGodosIrregulars(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bushido triggers only once when Ronin Cavekeeper becomes blocked by multiple creatures")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent ronin = addReadyRonin(player1);
        ronin.setAttacking(true);
        addReadyGodosIrregulars(player2);
        addReadyGodosIrregulars(player2);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0),
                    new BlockerAssignment(1, 0)));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(5);
    }

    private Permanent addReadyRonin(Player player) {
        return addCreatureReady(player, new RoninCavekeeper());
    }

    private Permanent addReadyGodosIrregulars(Player player) {
        return addCreatureReady(player, new GodosIrregulars());
    }
}
