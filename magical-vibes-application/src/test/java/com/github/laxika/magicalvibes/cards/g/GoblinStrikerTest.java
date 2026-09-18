package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuriokTransfixer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinStriker.class, AuriokTransfixer.class})
class GoblinStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackTheTurnItEnters() {
        harness.addToBattlefieldAndReturn(player1, new GoblinStriker());

        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("First strike deals combat damage before an equal-sized blocker")
    void firstStrikeDealsDamageFirst() {
        Permanent striker = addCreatureReady(player1, new GoblinStriker());
        striker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AuriokTransfixer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Striker");
        harness.assertInGraveyard(player2, "Auriok Transfixer");
    }
}
