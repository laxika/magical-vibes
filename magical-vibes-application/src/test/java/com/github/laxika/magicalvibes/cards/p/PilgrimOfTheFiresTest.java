package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PilgrimOfTheFiresTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets it survive combat while trample damages the defending player")
    void firstStrikeAndTrampleApplyInCombat() {
        harness.setLife(player2, 20);
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfTheFires());
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        pilgrim.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class))
                .isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pilgrim);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
