package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuntedWumpusTest {

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

    /**
     * Give player1 a Hunted Wumpus hand + enough mana, then cast it.
     * After casting the game auto-passes priority for player2 (no playable cards,
     * stack is non-empty so auto-pass doesn't fire), but we still have priority state.
     */
    private void setupAndCastWumpus() {
        harness.setHand(player1, List.of(new HuntedWumpus()));
        harness.addMana(player1, "G", 4);
        harness.castCreature(player1, 0);
    }

    @Test
    void castingWumpusPutsItOnStack() {
        harness.setHand(player1, List.of(new HuntedWumpus()));
        harness.addMana(player1, "G", 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();

        // Wumpus is on the stack as a creature spell
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Hunted Wumpus");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());

        // Wumpus is NOT on the battlefield yet
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(p1Battlefield).noneMatch(p -> p.getCard().getName().equals("Hunted Wumpus"));

        // Hand is now empty (had 1 card, played it)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        // Mana was spent ({3}{G} = 4 green total)
        assertThat(gd.playerManaPools.get(player1.getId()).get("G")).isEqualTo(0);
    }

    @Test
    void wumpusResolvesOntoBattlefield() {
        setupAndCastWumpus();

        // Both players pass priority → creature spell resolves
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature spell resolved → stack should have the ETB triggered ability
        assertThat(gd.stack).hasSize(1);
        StackEntry etbEntry = gd.stack.getFirst();
        assertThat(etbEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etbEntry.getCard().getName()).isEqualTo("Hunted Wumpus");

        // Wumpus IS on player1's battlefield
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(p1Battlefield).anyMatch(p -> p.getCard().getName().equals("Hunted Wumpus"));

        Permanent wumpus = p1Battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Hunted Wumpus"))
                .findFirst().orElseThrow();
        assertThat(wumpus.getCard().getPower()).isEqualTo(6);
        assertThat(wumpus.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    void wumpusEtbTriggersOpponentChoice() {
        setupAndCastWumpus();
        // Resolve creature spell → ETB goes on stack
        harness.passBothPriorities();

        // Give opponent a hand with a creature
        harness.setHand(player2, List.of(new GrizzlyBears()));

        // Resolve ETB → opponent should be asked to choose a card
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingCardChoice).isTrue();
        assertThat(gd.awaitingCardChoicePlayerId).isEqualTo(player2.getId());
        assertThat(gd.awaitingCardChoiceValidIndices).containsExactly(0);
    }

    @Test
    void opponentPutsCreatureViaWumpusEtb() {
        setupAndCastWumpus();
        harness.passBothPriorities(); // resolve creature spell

        // Give opponent a hand with [Forest, Grizzly Bears, Forest]
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));

        harness.passBothPriorities(); // resolve ETB → awaiting card choice

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingCardChoice).isTrue();
        // Valid indices should be [1] (Grizzly Bears is at index 1)
        assertThat(gd.awaitingCardChoiceValidIndices).containsExactly(1);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        // Opponent chooses Grizzly Bears (index 1)
        harness.handleCardChosen(player2, 1);

        // Grizzly Bears is on player2's battlefield
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(p2Battlefield).anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Opponent's hand decreased by 1
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore - 1);

        // Stack is empty (Grizzly Bears has no ETB)
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentDeclinesWumpusEtb() {
        setupAndCastWumpus();
        harness.passBothPriorities(); // resolve creature spell

        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.passBothPriorities(); // resolve ETB → awaiting card choice

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingCardChoice).isTrue();

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        int battlefieldSizeBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Opponent declines (index -1)
        harness.handleCardChosen(player2, -1);

        // No new creature on opponent's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldSizeBefore);

        // Opponent's hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    void wumpusEtbWithNoCreaturesInOpponentHand() {
        setupAndCastWumpus();
        harness.passBothPriorities(); // resolve creature spell

        // Give opponent a hand of only Forests (no creatures)
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));

        // Resolve ETB → should skip card choice since no creatures
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingCardChoice).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("has no creature cards in hand"));
    }

    @Test
    void wumpusEtbRecursiveOpponentPutsWumpus() {
        setupAndCastWumpus();
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        // Give opponent a hand with another Hunted Wumpus
        harness.setHand(player2, List.of(new HuntedWumpus()));

        harness.passBothPriorities(); // resolve ETB → awaiting card choice

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingCardChoice).isTrue();

        // Opponent puts their Wumpus
        harness.handleCardChosen(player2, 0);

        // The second Wumpus's ETB is now on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry recursiveEtb = gd.stack.getFirst();
        assertThat(recursiveEtb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(recursiveEtb.getCard().getName()).isEqualTo("Hunted Wumpus");

        // Both Wumpuses are on their respective battlefields
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hunted Wumpus"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hunted Wumpus"));
    }

    @Test
    void wumpusEtbDoubleRecursiveChain() {
        setupAndCastWumpus();
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        // Give opponent a Hunted Wumpus
        harness.setHand(player2, List.of(new HuntedWumpus()));

        harness.passBothPriorities(); // resolve ETB → awaiting card choice for player2

        // Player2 puts their Wumpus
        harness.handleCardChosen(player2, 0);

        GameData gd = harness.getGameData();

        // Second Wumpus's ETB is on the stack (controllerId = player2)
        assertThat(gd.stack).hasSize(1);
        StackEntry secondEtb = gd.stack.getFirst();
        assertThat(secondEtb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(secondEtb.getControllerId()).isEqualTo(player2.getId());

        // Give player1 a Grizzly Bears so they can use the second Wumpus's ETB
        harness.setHand(player1, List.of(new GrizzlyBears()));

        // Resolve second ETB → player1 (opponent of controller player2) is asked to choose
        harness.passBothPriorities();

        assertThat(gd.awaitingCardChoice).isTrue();
        assertThat(gd.awaitingCardChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.awaitingCardChoiceValidIndices).containsExactly(0);

        // Player1 puts their Grizzly Bears
        harness.handleCardChosen(player1, 0);

        // Grizzly Bears is on player1's battlefield (alongside the first Wumpus)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hunted Wumpus"))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Player2 has their Wumpus on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hunted Wumpus"));

        // Player1's hand is now empty (they put the Grizzly Bears)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        // Stack is empty (Grizzly Bears has no ETB, chain is done)
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastCreatureWhileStackNonEmpty() {
        setupAndCastWumpus();

        // Give player1 another creature + mana to try casting it
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, "G", 2);

        // Attempting to play while stack is non-empty should fail
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
