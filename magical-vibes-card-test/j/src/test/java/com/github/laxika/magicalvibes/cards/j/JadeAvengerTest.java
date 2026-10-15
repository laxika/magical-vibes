package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadeAvenger.class, GrizzlyBears.class})
class JadeAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Jade Avenger gets +2/+2 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent avenger = addCreatureReady(player1, new JadeAvenger());
        avenger.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(avenger.getPowerModifier()).isEqualTo(2);
        assertThat(avenger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Jade Avenger gets +2/+2 when it blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent avenger = addCreatureReady(player2, new JadeAvenger());

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(avenger.getPowerModifier()).isEqualTo(2);
        assertThat(avenger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Jade Avenger's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent avenger = addCreatureReady(player1, new JadeAvenger());
        avenger.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avenger.getPowerModifier()).isZero();
        assertThat(avenger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido triggers only once when multiple creatures block Jade Avenger")
    void multipleBlockersGiveOnlyOneBonus() {
        Permanent avenger = addCreatureReady(player1, new JadeAvenger());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(avenger.getPowerModifier()).isZero();
        assertThat(avenger.getToughnessModifier()).isZero();

        resolveAllTriggers();

        assertThat(avenger.getPowerModifier()).isEqualTo(2);
        assertThat(avenger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked Jade Avenger does not get a bushido bonus")
    void unblockedAttackDoesNotTriggerBushido() {
        Permanent avenger = addCreatureReady(player1, new JadeAvenger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(avenger.getPowerModifier()).isZero();
        assertThat(avenger.getToughnessModifier()).isZero();
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
