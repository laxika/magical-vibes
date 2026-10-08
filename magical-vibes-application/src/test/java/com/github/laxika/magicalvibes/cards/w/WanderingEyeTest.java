package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MoggToady;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingEye.class, MoggToady.class, RootwaterCommando.class})
class WanderingEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Wandering Eye in hand or graveyard does not reveal hands")
    void doesNotRevealHandsOutsideBattlefield() {
        harness.setHand(player1, List.of(new WanderingEye(), new RootwaterCommando()));
        harness.setHand(player2, List.of(new MoggToady()));
        harness.setGraveyard(player2, List.of(new WanderingEye()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .allMatch(message -> message.contains("\"opponentHand\":[]"))
                .isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .allMatch(message -> message.contains("\"opponentHand\":[]"))
                .isNotEmpty();
    }

    @Test
    @DisplayName("Revealed hands reflect newly added cards and cards leaving hand")
    void revealsCurrentHandContents() {
        harness.addToBattlefield(player1, new WanderingEye());
        harness.setHand(player2, List.of(new MoggToady()));
        harness.publishState();
        harness.setHand(player2, List.of(new RootwaterCommando()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .isNotEmpty()
                .allMatch(message -> message.contains("Rootwater Commando"))
                .noneMatch(message -> message.contains("Mogg Toady"));
    }

    @Test
    @DisplayName("Removing one Wandering Eye leaves both hands revealed while another remains")
    void remainingCopyKeepsHandsRevealed() {
        harness.addToBattlefield(player1, new WanderingEye());
        harness.addToBattlefield(player2, new WanderingEye());
        harness.setHand(player1, List.of(new RootwaterCommando()));
        harness.setHand(player2, List.of(new MoggToady()));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Mogg Toady"));
        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Rootwater Commando"));
    }

    @Test
    @DisplayName("Both players see each other's hands while Wandering Eye is on the battlefield")
    void bothHandsRevealed() {
        harness.addToBattlefield(player1, new WanderingEye());
        harness.setHand(player1, List.of(new RootwaterCommando()));
        harness.setHand(player2, List.of(new MoggToady()));
        harness.clearMessages();

        harness.passPriority(player1);

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Mogg Toady"));

        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Rootwater Commando"));
    }

    @Test
    @DisplayName("Both hands are revealed regardless of which player controls Wandering Eye")
    void bothHandsRevealedWhenOpponentControlsIt() {
        harness.addToBattlefield(player2, new WanderingEye());
        harness.setHand(player1, List.of(new RootwaterCommando()));
        harness.setHand(player2, List.of(new MoggToady()));
        harness.clearMessages();

        harness.passPriority(player1);

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Mogg Toady"));
        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Rootwater Commando"));
    }

    @Test
    @DisplayName("Hands are hidden after Wandering Eye leaves the battlefield")
    void handsHiddenAfterRemoval() {
        harness.addToBattlefield(player1, new WanderingEye());
        harness.setHand(player1, List.of(new RootwaterCommando()));
        harness.setHand(player2, List.of(new MoggToady()));

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> player2Messages = harness.getConn2().getMessagesContaining("\"opponentHand\"");
        assertThat(player2Messages).anyMatch(message -> message.contains("\"opponentHand\":[]"));
        assertThat(player2Messages).noneMatch(message -> message.contains("Rootwater Commando"));
    }
}
