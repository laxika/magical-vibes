package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZursWeirdingTest extends BaseCardTest {

    // ===== Draw replacement: any other player may pay 2 life =====

    @Test
    @DisplayName("Opponent pays 2 life to send the drawn card to its owner's graveyard")
    void opponentPaysLifeToDenyDraw() {
        harness.addToBattlefield(player1, new ZursWeirding());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The other player (player2) is asked whether to pay 2 life.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the payment lets the drawing player draw the revealed card")
    void decliningLetsPlayerDraw() {
        harness.addToBattlefield(player1, new ZursWeirding());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draw happens normally when the other player can't pay 2 life")
    void drawsNormallyWhenOpponentCannotPay() {
        harness.addToBattlefield(player1, new ZursWeirding());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setLife(player2, 1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // No payment is possible — the drawing player simply draws, opponent's life is untouched.
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 1);
    }

    // ===== Static: players play with their hands revealed =====

    @Test
    @DisplayName("Both players see each other's hands while Zur's Weirding is on the battlefield")
    void bothHandsRevealed() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Controller of Zur's Weirding sees the opponent's hand.
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));

        // The opponent also sees the controller's hand (unlike Telepathy).
        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Hands are no longer revealed after Zur's Weirding leaves the battlefield")
    void handsHiddenAfterRemoval() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p2Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
    }
}
