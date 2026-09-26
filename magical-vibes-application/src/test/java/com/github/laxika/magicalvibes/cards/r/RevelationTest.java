package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Revelation.class, AzureDrake.class, BarbaryApes.class})
class RevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Both players see each other's hands while Revelation is on the battlefield")
    void bothHandsRevealed() {
        harness.addToBattlefield(player1, new Revelation());
        harness.setHand(player1, List.of(new AzureDrake()));
        harness.setHand(player2, List.of(new BarbaryApes()));
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> player1Messages = harness.getConn1().getSentMessages();
        assertThat(player1Messages).anyMatch(message ->
                message.contains("\"opponentHand\"") && message.contains("Barbary Apes"));

        List<String> player2Messages = harness.getConn2().getSentMessages();
        assertThat(player2Messages).anyMatch(message ->
                message.contains("\"opponentHand\"") && message.contains("Azure Drake"));
    }

    @Test
    @DisplayName("Hands are hidden after Revelation leaves the battlefield")
    void handsHiddenAfterRemoval() {
        harness.addToBattlefield(player1, new Revelation());
        harness.setHand(player1, List.of(new AzureDrake()));
        harness.setHand(player2, List.of(new BarbaryApes()));

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> player2Messages = harness.getConn2().getSentMessages();
        assertThat(player2Messages).anyMatch(message -> message.contains("\"opponentHand\":[]"));
        assertThat(player2Messages).noneMatch(message ->
                message.contains("\"opponentHand\"") && message.contains("Azure Drake"));
    }
}
