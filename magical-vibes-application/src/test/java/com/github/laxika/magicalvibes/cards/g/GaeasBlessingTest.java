package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CallOfTheWild;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SilentGravestone;
import com.github.laxika.magicalvibes.cards.s.ShalaiVoiceOfPlenty;
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

@CardUsed({GaeasBlessing.class, CallOfTheWild.class, MindStone.class, Millstone.class, FuneralCharm.class,
        SilentGravestone.class, ShalaiVoiceOfPlenty.class})
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
    @DisplayName("Only selected graveyard cards are shuffled into the library")
    void unselectedGraveyardCardsRemain() {
        Card selected = new MindStone();
        Card unselected = new CallOfTheWild();
        harness.setGraveyard(player2, List.of(selected, unselected));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player2.getId())).contains(selected).hasSize(librarySizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Graveyard targets made illegal before resolution remain in the graveyard, but caster draws")
    void graveyardTargetsMadeUntargetableAreNotShuffled() {
        Card selected = new MindStone();
        harness.setGraveyard(player2, List.of(selected));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.addToBattlefield(player2, new SilentGravestone());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Gaea's Blessing");
    }

    @Test
    @DisplayName("Caster still draws when the player target becomes illegal but a graveyard target remains legal")
    void illegalPlayerTargetDoesNotPreventDrawWithLegalCardTarget() {
        Card selected = new MindStone();
        harness.setGraveyard(player2, List.of(selected));
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Gaea's Blessing");
    }

    @Test
    @DisplayName("Each milled Blessing triggers and resolves even after the first trigger empties the graveyard")
    void multipleMilledBlessingsTriggerIndependently() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card first = new GaeasBlessing();
        Card second = new GaeasBlessing();
        harness.setLibrary(player2, List.of(first, second));
        harness.addToBattlefield(player2, new SilentGravestone());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).isEmpty();
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
        harness.setLibrary(player2, List.of(new GaeasBlessing(), new MindStone()));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

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
        harness.setLibrary(player2, List.of(new GaeasBlessing(), new MindStone()));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        // Both milled cards were shuffled back into library (trigger fires after both enter graveyard)
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discarding Gaea's Blessing does not trigger its library ability")
    void noTriggerOnDiscard() {
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new FuneralCharm(), new GaeasBlessing()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Gaea's Blessing");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When put into the graveyard from the library without being milled, shuffles the graveyard")
    void triggerFiresWhenPutIntoGraveyardFromLibraryWithoutMilling() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        harness.setLibrary(player1, List.of(new GaeasBlessing(), new MindStone()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

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
        harness.setLibrary(player2, List.of(new MindStone(), new GaeasBlessing()));

        // Put something in player2's graveyard
        harness.setGraveyard(player2, List.of(new CallOfTheWild()));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        // Graveyard should be empty — trigger shuffled everything back
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // All cards should be in library (1 existing graveyard + 2 milled = 3)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
}
