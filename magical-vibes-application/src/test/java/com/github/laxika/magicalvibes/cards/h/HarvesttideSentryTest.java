package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarvesttideSentry.class, GrizzlyBears.class, CrawWurm.class, HillGiant.class})
class HarvesttideSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Coven prevents power 2 or less creatures from blocking")
    void covenPreventsLowPowerBlockers() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Coven allows creatures with power 3 or greater to block")
    void covenAllowsHighPowerBlockers() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        advanceToCombat(player1);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Without Coven, power 2 creatures can block")
    void doesNotRestrictBlockersWithoutCoven() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    private Permanent addReadySentry() {
        return addCreatureReady(player1, new HarvesttideSentry());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
