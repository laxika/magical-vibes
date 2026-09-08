package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruPathwarden.class, GrizzlyBears.class})
class TajuruPathwardenTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent pathwarden = addCreatureReady(player1, new TajuruPathwarden());

        declareAttackers(List.of(0));

        assertThat(pathwarden.isTapped()).isFalse();
    }

    @Test
    void trampleDealsExcessCombatDamageToTheDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent pathwarden = addCreatureReady(player1, new TajuruPathwarden());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        pathwarden.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
