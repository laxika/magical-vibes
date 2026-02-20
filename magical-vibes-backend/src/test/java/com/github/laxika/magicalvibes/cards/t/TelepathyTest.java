package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelepathyTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Telepathy has correct card properties")
    void hasCorrectProperties() {
        Telepathy card = new Telepathy();

        assertThat(card.getName()).isEqualTo("Telepathy");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getCardText()).isEqualTo("Your opponents play with their hands revealed.");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(RevealOpponentHandsEffect.class);
    }

    // ===== Casting and resolving =====

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
        assertThat(entry.getCard().getName()).isEqualTo("Telepathy");
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Telepathy"));
    }

    // ===== Static ability: reveal opponent's hand =====

    @Test
    @DisplayName("Controller sees opponent's hand in game state when Telepathy is on the battlefield")
    void controllerSeesOpponentHand() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        // Trigger a game state broadcast
        harness.passPriority(player1);

        // Player1 (Telepathy controller) should see opponent's hand cards in the broadcast
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent does not see controller's hand without their own Telepathy")
    void opponentDoesNotSeeControllerHand() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Player2 should have an empty opponentHand
        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        // Player2 should not see Air Elemental in opponentHand
        assertThat(p2Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Opponent hand is empty in game state when Telepathy is not on the battlefield")
    void noTelepathyMeansNoReveal() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Player1 should have an empty opponentHand
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Revealed hand updates when opponent's hand changes")
    void revealedHandUpdatesOnHandChange() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Player1 should see both cards
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears") && m.contains("Air Elemental"));

        // Change opponent's hand
        harness.setHand(player2, List.of(new AirElemental()));
        harness.clearMessages();

        harness.passPriority(player2);

        // Player1 should now see only Air Elemental
        p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Revealed hand is empty when opponent has no cards in hand")
    void revealedHandEmptyWhenOpponentHandEmpty() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of());
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
    }

    @Test
    @DisplayName("Both players see each other's hands when both have Telepathy")
    void bothPlayersWithTelepathy() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.addToBattlefield(player2, new Telepathy());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Player1 sees Grizzly Bears
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));

        // Player2 sees Air Elemental
        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Opponent hand is no longer revealed after Telepathy leaves the battlefield")
    void handNotRevealedAfterTelepathyRemoved() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Verify hand is revealed
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));

        // Remove Telepathy from battlefield
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passPriority(player2);

        // Hand should no longer be revealed
        p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Telepathy reveals opponent's hand after being cast and resolved")
    void revealsHandAfterCasting() {
        harness.setHand(player1, List.of(new Telepathy()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.clearMessages();
        harness.passBothPriorities();

        // After resolving, the game state should include the revealed hand
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));
    }
}

