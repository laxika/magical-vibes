package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Telepathy.class, CoralMerfolk.class, GorillaWarrior.class})
class TelepathyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Telepathy puts it on the stack as an enchantment spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Telepathy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);

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
        harness.setHand(player1, List.of(new Telepathy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Telepathy");
    }

    @Test
    @DisplayName("Controller sees opponent's hand in game state when Telepathy is on the battlefield")
    void controllerSeesOpponentHand() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.clearMessages();

        harness.publishState();

        // Player1 (Telepathy controller) should see opponent's hand cards in the broadcast
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));
    }

    @Test
    @DisplayName("Opponent does not see controller's hand without their own Telepathy")
    void opponentDoesNotSeeControllerHand() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player1, List.of(new GorillaWarrior()));
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.clearMessages();

        harness.publishState();

        // Player2 should have an empty opponentHand
        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        // Player2 should not see Gorilla Warrior in opponentHand
        assertThat(p2Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Gorilla Warrior"));
    }

    @Test
    @DisplayName("Opponent hand is empty in game state when Telepathy is not on the battlefield")
    void noTelepathyMeansNoReveal() {
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.clearMessages();

        harness.publishState();

        // Player1 should have an empty opponentHand
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));
    }

    @Test
    @DisplayName("Revealed hand updates when opponent's hand changes")
    void revealedHandUpdatesOnHandChange() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new CoralMerfolk(), new GorillaWarrior()));
        harness.clearMessages();

        harness.publishState();

        // Player1 should see both cards
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"")
                && m.contains("Coral Merfolk") && m.contains("Gorilla Warrior"));

        // Change opponent's hand
        harness.setHand(player2, List.of(new GorillaWarrior()));
        harness.clearMessages();

        harness.publishState();

        // Player1 should now see only Gorilla Warrior
        p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Gorilla Warrior"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));
    }

    @Test
    @DisplayName("Revealed hand is empty when opponent has no cards in hand")
    void revealedHandEmptyWhenOpponentHandEmpty() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of());
        harness.clearMessages();

        harness.publishState();

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
    }

    @Test
    @DisplayName("Both players see each other's hands when both have Telepathy")
    void bothPlayersWithTelepathy() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.addToBattlefield(player2, new Telepathy());
        harness.setHand(player1, List.of(new GorillaWarrior()));
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.clearMessages();

        harness.publishState();

        // Player1 sees Coral Merfolk
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));

        // Player2 sees Gorilla Warrior
        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Gorilla Warrior"));
    }

    @Test
    @DisplayName("Opponent hand is no longer revealed after Telepathy leaves the battlefield")
    void handNotRevealedAfterTelepathyRemoved() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.clearMessages();

        harness.publishState();

        // Verify hand is revealed
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));

        // Remove Telepathy from battlefield
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        // Hand should no longer be revealed
        p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));
    }

    @Test
    @DisplayName("Telepathy does not reveal the opponent's hand while it is on the stack")
    void handIsNotRevealedBeforeResolution() {
        harness.setHand(player1, List.of(new Telepathy()));
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.clearMessages();
        harness.publishState();

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));
    }

    @Test
    @DisplayName("Telepathy reveals opponent's hand after being cast and resolved")
    void revealsHandAfterCasting() {
        harness.setHand(player1, List.of(new Telepathy()));
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.clearMessages();
        harness.passBothPriorities();

        // After resolving, the game state should include the revealed hand
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Coral Merfolk"));
    }
}

