package com.github.laxika.magicalvibes.cards.b;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleMadRonin.class, WanderingOnes.class})
class BattleMadRoninTest extends BaseCardTest {

    @Test
    @DisplayName("When Battle-Mad Ronin becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new BattleMadRonin());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(2);
        assertThat(ronin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Battle-Mad Ronin blocks, it gets +2/+2 until end of turn")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent ronin = addCreatureReady(player2, new BattleMadRonin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(2);
        assertThat(ronin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Battle-Mad Ronin is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new BattleMadRonin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Battle-Mad Ronin becomes blocked by multiple creatures, it gets only one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new BattleMadRonin());
        addCreatureReady(player2, new WanderingOnes());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(ronin.getPowerModifier()).isEqualTo(2);
        assertThat(ronin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Battle-Mad Ronin's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent ronin = addCreatureReady(player1, new BattleMadRonin());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(2);
        assertThat(ronin.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Declaring no attackers while Battle-Mad Ronin can attack is illegal")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new BattleMadRonin());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Declaring Battle-Mad Ronin as an attacker is legal")
    void canAttack() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BattleMadRonin());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
