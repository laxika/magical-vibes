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

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(leader.getId())).isNotNull();
    }

    @Test
    @DisplayName("A warped Team Leader can be cast on a later turn without another exile")
    void castsWarpedLeaderOnLaterTurn() {
        MemorialTeamLeader leader = new MemorialTeamLeader();
        harness.setHand(player1, List.of(leader));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(leader.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, leader.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Memorial Team Leader");
        assertThat(gd.findExiledCard(leader.getId())).isNull();
    }

    @Test
    @DisplayName("Two Team Leaders boost each other only during their controller's turn")
    void leadersBoostEachOtherWithoutBoostingThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MemorialTeamLeader());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MemorialTeamLeader());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MemorialTeamLeader());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);
    }
}
