package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirElemental.class, GrizzlyBears.class, Telepathy.class})
class TelepathyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Telepathy puts it on the stack as an enchantment spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Telepathy(), "{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Telepathy resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new Telepathy(), "{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Telepathy");
    }

    @Test
    @DisplayName("Controller sees opponent's hand in game state when Telepathy is on the battlefield")
    void controllerSeesOpponentHand() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Opponent does not see controller's hand without their own Telepathy")
    void opponentDoesNotSeeControllerHand() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\":[]")).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent hand is empty in game state when Telepathy is not on the battlefield")
    void noTelepathyMeansNoReveal() {
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\":[]")).isNotEmpty();
        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .noneMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Revealed hand updates when opponent's hand changes")
    void revealedHandUpdatesOnHandChange() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new AirElemental(), new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        List<String> p1Messages = harness.getConn1().getMessagesContaining("\"opponentHand\"");
        assertThat(p1Messages).anyMatch(message -> message.contains("Air Elemental")
                && message.contains("Grizzly Bears"));

        // Change opponent's hand
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        // Player1 should now see only Grizzly Bears
        p1Messages = harness.getConn1().getMessagesContaining("\"opponentHand\"");
        assertThat(p1Messages).anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(p1Messages).noneMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Revealed hand is empty when opponent has no cards in hand")
    void revealedHandEmptyWhenOpponentHandEmpty() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of());
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\":[]")).isNotEmpty();
    }

    @Test
    @DisplayName("Both players see each other's hands when both have Telepathy")
    void bothPlayersWithTelepathy() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.addToBattlefield(player2, new Telepathy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Air Elemental"));

        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Telepathy reveals the opponent's hand to its controller regardless of controller")
    void opponentControllerSeesMyHand() {
        harness.addToBattlefield(player2, new Telepathy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\":[]")).isNotEmpty();
        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .noneMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Opponent hand is no longer revealed after Telepathy leaves the battlefield")
    void handNotRevealedAfterTelepathyRemoved() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Air Elemental"));

        // Remove Telepathy from battlefield
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\":[]")).isNotEmpty();
        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .noneMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Telepathy does not reveal the opponent's hand while it has lost its abilities")
    void abilityLossDisablesHandReveal() {
        var telepathy = harness.addToBattlefieldAndReturn(player1, new Telepathy());
        telepathy.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> messages = harness.getConn1().getMessagesContaining("\"opponentHand\"");
        assertThat(messages).anyMatch(message -> message.contains("\"opponentHand\":[]"));
        assertThat(messages).noneMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Telepathy reveals opponent's hand after being cast and resolved")
    void revealsHandAfterCasting() {
        harness.setHand(player2, List.of(new AirElemental()));
        harness.castFromHand(player1, new Telepathy(), "{U}");
        harness.clearMessages();
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Telepathy reveals opponent's hand after being cast and resolved")
    void revealsHandAfterCastingUpstreamReview() {
        harness.setHand(player2, List.of(new AirElemental()));
        harness.castFromHand(player1, new Telepathy(), "{U}");
        harness.clearMessages();
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Telepathy does not reveal the opponent's hand while it is on the stack")
    void handIsNotRevealedBeforeResolution() {
        harness.setHand(player2, List.of(new AirElemental()));
        harness.castFromHand(player1, new Telepathy(), "{U}");
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\":[]")).isNotEmpty();
        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .noneMatch(message -> message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("An opponent's hand remains revealed while another Telepathy remains")
    void remainingCopyKeepsHandRevealed() {
        var firstCopy = harness.addToBattlefieldAndReturn(player1, new Telepathy());
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new AirElemental()));

        harness.getGameData().playerBattlefields.get(player1.getId()).remove(firstCopy);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Air Elemental"));
    }
}
