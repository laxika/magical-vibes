package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WardOfBonesTest extends BaseCardTest {

    // ===== Creature clause: opponent controlling more creatures can't cast creature spells =====

    @Test
    @DisplayName("Opponent controlling more creatures can't cast creature spells")
    void opponentWithMoreCreaturesCantCastCreatureSpells() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Player1 controls one creature, player2 (Ward's controller) controls none.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent with equal creature count can still cast creature spells")
    void opponentWithEqualCreaturesCanCastCreatureSpells() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Both players control one creature — not "more than", so no restriction.
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Ward of Bones controller is never restricted, even with more creatures")
    void controllerNotRestricted() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Controller (player2) has more creatures than the opponent — restriction is opponent-only.
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        assertThat(gbs.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    // ===== Land clause: opponent controlling more lands can't play lands =====

    @Test
    @DisplayName("Opponent controlling more lands can't play lands")
    void opponentWithMoreLandsCantPlayLands() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Player1 controls one land, player2 controls none.
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent with equal land count can still play lands")
    void opponentWithEqualLandsCanPlayLands() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Both players control one land — not "more than", so no restriction.
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }
}
