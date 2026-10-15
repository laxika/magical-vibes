package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Plagiarize.class, DeepAnalysis.class, PossessedAven.class, ThoughtReflection.class})
class PlagiarizeTest extends BaseCardTest {

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
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target a permanent with Plagiarize")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new PossessedAven());
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Possessed Aven")))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Replacement effect setup =====

    @Test
    @DisplayName("Resolving Plagiarize sets up draw replacement for target player")
    void resolvingSetsUpDrawReplacement() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.drawReplacementTargetToController).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("Plagiarize goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Plagiarize");
    }

    // ===== Draw step replacement =====

    @Test
    @DisplayName("Plagiarize replaces opponent's draw step draw — controller draws instead")
    void replacesDrawStepDraw() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Set up: player2 is active, about to draw
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2; // Avoid first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        // Advance from UPKEEP to DRAW — this triggers handleDrawStep
        harness.passUntil(player2, TurnStep.DRAW);

        // Player2's hand should not increase (draw was skipped)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
        // Player2's deck should not decrease
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
        // Player1 should have drawn a card instead
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 1);
    }

    @Test
    @DisplayName("Plagiarize replaces an empty-library draw before the target loses")
    void replacesEmptyLibraryDraw() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        gd.playerDecks.get(player2.getId()).clear();

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(player2, TurnStep.DRAW);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
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
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Reset state so player2 can cast
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Now have player2 cast a draw spell that makes its target draw two cards.
        DeepAnalysis deepAnalysis = new DeepAnalysis();
        harness.setHand(player2, List.of(deepAnalysis));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player2, 0, player2.getId());

        // Deep Analysis makes player2 draw two cards, but Plagiarize replaces both draws,
        // so player1 draws two cards instead.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 2);
    }

    @Test
    @DisplayName("Plagiarize's replacement draw can be modified by the controller's draw replacements")
    void replacementDrawUsesControllersDrawReplacements() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.setLibrary(player1, List.of(
                new DeepAnalysis(), new DeepAnalysis(), new DeepAnalysis(), new DeepAnalysis()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DeepAnalysis()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player2, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 4);
    }

    @Test
    @DisplayName("Opposing Plagiarizes redirect a draw twice without reapplying either effect")
    void opposingPlagiarizesApplyOnceEach() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Plagiarize()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.setHand(player2, List.of(new DeepAnalysis()));
        harness.setLibrary(player1, List.of(new DeepAnalysis(), new DeepAnalysis()));
        harness.setLibrary(player2, List.of(new DeepAnalysis(), new DeepAnalysis()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player2, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Plagiarize does not replace draws by the untargeted player")
    void untargetedPlayersDrawsAreUnaffected() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DeepAnalysis()));
        harness.setLibrary(player1, List.of(new DeepAnalysis(), new DeepAnalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Plagiarize replacement effect is cleared at cleanup step")
    void replacementClearedAtCleanup() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.drawReplacementTargetToController).isNotEmpty();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gd.drawReplacementTargetToController).isEmpty();
    }

    // ===== Can target self =====

    @Test
    @DisplayName("Can target self with Plagiarize")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.drawReplacementTargetToController).containsEntry(player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("Targeting self with Plagiarize still allows own draws (controller draws)")
    void targetingSelfStillAllowsOwnDraws() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        // Player1 is both target and controller — draw step should still give player1 a card
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player1, TurnStep.DRAW);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving Plagiarize logs the replacement setup")
    void logsReplacementSetup() {
        harness.setHand(player1, List.of(new Plagiarize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Plagiarize") && log.contains("draws are replaced"));
    }
}

