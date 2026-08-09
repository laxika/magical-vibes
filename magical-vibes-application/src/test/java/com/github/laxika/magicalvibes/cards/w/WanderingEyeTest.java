package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WanderingEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Both players see each other's hands while Wandering Eye is on the battlefield")
    void bothHandsRevealed() {
        harness.addToBattlefield(player1, new WanderingEye());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> player1Messages = harness.getConn1().getSentMessages();
        assertThat(player1Messages).anyMatch(message ->
                message.contains("\"opponentHand\"") && message.contains("Grizzly Bears"));

        List<String> player2Messages = harness.getConn2().getSentMessages();
        assertThat(player2Messages).anyMatch(message ->
                message.contains("\"opponentHand\"") && message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Hands are hidden after Wandering Eye leaves the battlefield")
    void handsHiddenAfterRemoval() {
        harness.addToBattlefield(player1, new WanderingEye());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> player2Messages = harness.getConn2().getSentMessages();
        assertThat(player2Messages).anyMatch(message -> message.contains("\"opponentHand\":[]"));
        assertThat(player2Messages).noneMatch(message ->
                message.contains("\"opponentHand\"") && message.contains("Air Elemental"));
    }
}
