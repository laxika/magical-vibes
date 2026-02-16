package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlagiarizeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Plagiarize has correct card properties")
    void hasCorrectProperties() {
        Plagiarize card = new Plagiarize();

        assertThat(card.getName()).isEqualTo("Plagiarize");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(RedirectDrawsEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Plagiarize puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Plagiarize");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    // ===== Replacement effect setup =====

    @Test
    @DisplayName("Resolving Plagiarize sets up draw replacement for target player")
    void resolvingSetsUpDrawReplacement() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.drawReplacementTargetToController).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("Plagiarize goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plagiarize"));
    }

    // ===== Draw step replacement =====

    @Test
    @DisplayName("Plagiarize replaces opponent's draw step draw — controller draws instead")
    void replacesDrawStepDraw() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Set up: player2 is active, about to draw
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2; // Avoid first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        // Advance from UPKEEP to DRAW — this triggers handleDrawStep
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player2's hand should not increase (draw was skipped)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
        // Player2's deck should not decrease
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
        // Player1 should have drawn a card instead
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 1);
    }

    // ===== Effect draw replacement =====

    @Test
    @DisplayName("Plagiarize replaces effect-based draws for target player")
    void replacesEffectDraws() {
        // Cast Plagiarize targeting player2
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Reset state so player2 can cast
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Now have player2 cast a draw spell (Peek targeting player1 to look at hand, also draws a card)
        Peek peek = new Peek();
        harness.setHand(player2, List.of(peek));
        harness.addMana(player2, ManaColor.BLUE, 1);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player2, 0, player1.getId()); // Peek targets player1 to look at hand
        harness.passBothPriorities();

        // Peek's DrawCardEffect draws for the controller (player2),
        // but Plagiarize replaces player2's draw, so player1 draws instead
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 1);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Plagiarize replacement effect is cleared at cleanup step")
    void replacementClearedAtCleanup() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.drawReplacementTargetToController).isNotEmpty();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.drawReplacementTargetToController).isEmpty();
    }

    // ===== Can target self =====

    @Test
    @DisplayName("Can target self with Plagiarize")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.drawReplacementTargetToController).containsEntry(player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("Targeting self with Plagiarize still allows own draws (controller draws)")
    void targetingSelfStillAllowsOwnDraws() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Player1 is both target and controller — draw step should still give player1 a card
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving Plagiarize logs the replacement setup")
    void logsReplacementSetup() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Plagiarize") && log.contains("draws are replaced"));
    }
}
