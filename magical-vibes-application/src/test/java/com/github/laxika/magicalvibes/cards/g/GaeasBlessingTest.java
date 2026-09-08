package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CallOfTheWild;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasBlessing.class, CallOfTheWild.class, MindStone.class, Millstone.class})
class GaeasBlessingTest extends BaseCardTest {

    // ===== Casting — graveyard targeting + draw =====

    @Test
    @DisplayName("Casting targeting self with cards in graveyard prompts for target selection")
    void castingTargetingSelfPromptsForGraveyardSelection() {
        Card card1 = new CallOfTheWild();
        Card card2 = new MindStone();
        harness.setGraveyard(player1, List.of(card1, card2));
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2); // min(3, 2 cards)
    }

    @Test
    @DisplayName("Resolving shuffles selected cards from graveyard into library and draws a card")
    void resolvingShufflesCardsAndDrawsCard() {
        Card card1 = new CallOfTheWild();
        Card card2 = new MindStone();
        harness.setGraveyard(player1, List.of(card1, card2));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        // Select both cards
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        // Resolve
        harness.passBothPriorities();

        // Cards should no longer be in graveyard (only Gaea's Blessing itself goes to graveyard)
        harness.assertNotInGraveyard(player1, "Call of the Wild");
        harness.assertNotInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Gaea's Blessing");

        // Library should have gained 2 cards (shuffled back) minus 1 drawn
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2 - 1);

        // Hand should have 1 card (cast 1 from hand, drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target opponent's graveyard and shuffle their cards, but controller draws")
    void canTargetOpponentGraveyard() {
        Card card1 = new CallOfTheWild();
        Card card2 = new MindStone();
        harness.setGraveyard(player2, List.of(card1, card2));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());

        // Select both cards
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Opponent's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Opponent's library gained 2 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);

        // Controller drew a card (cast 1 from hand, drew 1 → hand has 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        // Gaea's Blessing goes to caster's graveyard
        harness.assertInGraveyard(player1, "Gaea's Blessing");
    }

    @Test
    @DisplayName("Casting with empty target graveyard puts spell on stack directly and draws a card")
    void castingWithEmptyGraveyardStillDraws() {
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Controller drew 1 card (cast 1 from hand, drew 1 → hand has 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can choose zero graveyard cards and still shuffle the library and draw")
    void canChooseZeroGraveyardCardsAndStillDraw() {
        harness.setGraveyard(player1, List.of(new MindStone()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mind Stone", "Gaea's Blessing");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Max targets is capped at 3 even with more cards in graveyard")
    void maxTargetsCappedAtThree() {
        harness.setGraveyard(player1, List.of(
                new CallOfTheWild(), new MindStone(),
                new CallOfTheWild(), new MindStone()));
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(4);
    }

    // ===== Self-mill trigger =====

    @Test
    @DisplayName("When milled, shuffles owner's graveyard into library")
    void selfMillTriggerShufflesGraveyardIntoLibrary() {
        addCreatureReady(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Put a card in player2's graveyard first
        Card existingGraveyardCard = new CallOfTheWild();
        harness.setGraveyard(player2, List.of(existingGraveyardCard));

        // Set up player2's library: Gaea's Blessing on top, another card below
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GaeasBlessing());
        gd.playerDecks.get(player2.getId()).add(new MindStone());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Graveyard should be empty — everything was shuffled into library
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Library should contain the existing graveyard card + the milled cards (all shuffled back)
        // Original: 2 cards in library, milled 2. The Call of the Wild was in graveyard.
        // After mill: Gaea's Blessing and Mind Stone go to graveyard, trigger fires,
        // all 3 graveyard cards (existing + 2 milled) shuffle into library.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);

        // Log confirms the self-mill trigger and its resolution
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Gaea's Blessing") && log.contains("triggers"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("shuffles their graveyard"));
    }

    @Test
    @DisplayName("When milled with empty graveyard (only self), still shuffles into library")
    void selfMillTriggerWithOnlyItselfInGraveyard() {
        addCreatureReady(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Player2 has empty graveyard, library has Gaea's Blessing + another card
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GaeasBlessing());
        gd.playerDecks.get(player2.getId()).add(new MindStone());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both milled cards were shuffled back into library (trigger fires after both enter graveyard)
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Self-mill trigger does not fire when Gaea's Blessing is discarded (only from library)")
    void noTriggerOnDiscard() {
        // Gaea's Blessing in graveyard via discard (simulated by setGraveyard) — not milled
        Card existingCard = new MindStone();
        harness.setGraveyard(player1, List.of(new GaeasBlessing(), existingCard));

        // Graveyard should remain intact — no shuffle trigger
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Gaea's Blessing");
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("When put into the graveyard from the library without being milled, shuffles the graveyard")
    void triggerFiresWhenPutIntoGraveyardFromLibraryWithoutMilling() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        harness.setLibrary(player1, List.of(new GaeasBlessing(), new MindStone()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Gaea's Blessing", "Mind Stone");
    }

    @Test
    @DisplayName("Self-mill trigger fires even when milled by opponent")
    void selfMillTriggerFiresWhenMilledByOpponent() {
        addCreatureReady(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Set up player2's library with Gaea's Blessing
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new MindStone());
        gd.playerDecks.get(player2.getId()).add(new GaeasBlessing());

        // Put something in player2's graveyard
        harness.setGraveyard(player2, List.of(new CallOfTheWild()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Graveyard should be empty — trigger shuffled everything back
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // All cards should be in library (1 existing graveyard + 2 milled = 3)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
}
