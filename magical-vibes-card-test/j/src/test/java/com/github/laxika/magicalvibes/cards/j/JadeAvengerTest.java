package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

        assertThat(PermanentGrants.powerBoostOf(gd, avenger)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, avenger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jade Avenger gets +2/+2 when it blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent avenger = addCreatureReady(player2, new JadeAvenger());

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(PermanentGrants.powerBoostOf(gd, avenger)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, avenger)).isEqualTo(2);
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

        assertThat(PermanentGrants.powerBoostOf(gd, avenger)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, avenger)).isZero();
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
        assertThat(PermanentGrants.powerBoostOf(gd, avenger)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, avenger)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, avenger)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, avenger)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked Jade Avenger does not get a bushido bonus")
    void unblockedAttackDoesNotTriggerBushido() {
        Permanent avenger = addCreatureReady(player1, new JadeAvenger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, avenger)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, avenger)).isZero();
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
