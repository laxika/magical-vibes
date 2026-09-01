package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MemorialTeamLeader.class, GrizzlyBears.class})
class MemorialTeamLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other creatures its controller controls during their turn")
    void boostsOtherOwnCreaturesDuringControllerTurn() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new MemorialTeamLeader());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stops boosting other creatures during its controller's opponent's turn")
    void doesNotBoostDuringOpponentTurn() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new MemorialTeamLeader());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast for its warp cost and is exiled at the next end step")
    void canBeWarpedAndExilesAtNextEndStep() {
        MemorialTeamLeader leader = new MemorialTeamLeader();
        harness.setHand(player1, List.of(leader));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Memorial Team Leader");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(leader.getId())).isNotNull();
    }
}
