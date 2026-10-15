package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RooftopPercher.class, Plains.class})
class RooftopPercherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rooftop Percher puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new RooftopPercher(), "{5}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving creature prompts for graveyard target selection before ability goes on stack")
    void resolvingCreaturePromptsTargetSelection() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → ETB triggers → target selection prompt

        // Rooftop Percher is on the battlefield
        harness.assertOnBattlefield(player1, "Rooftop Percher");

        // Graveyard target selection is pending (at trigger time, before ability goes on stack)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);

        // ETB ability is NOT yet on the stack (waiting for target selection)
        assertThat(gd.stack).isEmpty();

        // Life has NOT been gained yet (happens on resolution)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Selecting targets puts ETB ability on stack, which resolves with exile and life gain")
    void selectingTargetsResolvesExileAndLifeGain() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        // Pick two card IDs from the valid set
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        List<UUID> chosenIds = validIds.subList(0, 2);

        int totalGraveyardBefore = gd.playerGraveyards.get(player1.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size();

        // Select targets → ability goes on stack
        harness.handleMultipleCardsChosen(player1, chosenIds);
        harness.passBothPriorities(); // resolve ETB → exile + life gain

        // Total graveyard cards reduced by 2
        int totalGraveyardAfter = gd.playerGraveyards.get(player1.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size();
        assertThat(totalGraveyardAfter).isEqualTo(totalGraveyardBefore - 2);

        // Total exiled cards increased by 2
        int totalExiled = gd.getPlayerExiledCards(player1.getId()).size()
                + gd.getPlayerExiledCards(player2.getId()).size();
        assertThat(totalExiled).isEqualTo(2);

        // Life was gained on resolution
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);

        // Awaiting state is cleared
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Log mentions exile
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("exiles") && entry.contains("from graveyard"));
    }

    @Test
    @DisplayName("Can exile cards from opponent's graveyard")
    void canExileFromOpponentGraveyard() {
        // Only opponent has graveyard cards
        harness.setGraveyard(player2, List.of(new RooftopPercher(), new Plains()));
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // All valid IDs should be from player2's graveyard
        List<UUID> p2GraveyardIds = gd.playerGraveyards.get(player2.getId()).stream()
                .map(Card::getId).toList();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).containsAll(p2GraveyardIds);

        // Exile both → ability goes on stack
        harness.handleMultipleCardsChosen(player1, p2GraveyardIds);
        harness.passBothPriorities(); // resolve ETB → exile + life gain

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Can exile one card when choosing fewer than maximum")
    void canExileFewerThanMax() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());

        // Choose only one card → ability goes on stack
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));
        harness.passBothPriorities(); // resolve ETB → exile + life gain

        int totalExiled = gd.getPlayerExiledCards(player1.getId()).size()
                + gd.getPlayerExiledCards(player2.getId()).size();
        assertThat(totalExiled).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing zero targets gains life but exiles nothing")
    void choosingZeroTargetsGainsLifeOnly() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        // Choose 0 targets (allowed by "up to") → ability goes on stack with no targets
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities(); // resolve ETB → life gain only

        // No cards exiled
        int totalExiled = gd.getPlayerExiledCards(player1.getId()).size()
                + gd.getPlayerExiledCards(player2.getId()).size();
        assertThat(totalExiled).isEqualTo(0);

        // Life was still gained (ability resolves normally with 0 targets)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB with empty graveyards skips target prompt, still gains life")
    void emptyGraveyardsStillGainsLife() {
        // No graveyard setup — graveyards are empty
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → ETB goes on stack with 0 targets (no prompt)
        harness.passBothPriorities(); // resolve ETB → gain life

        // Life was gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);

        // No graveyard choice was needed
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Max count is capped to available cards when fewer than 2 in graveyards")
    void maxCountCappedToAvailableCards() {
        // Only one card in graveyards total
        harness.setGraveyard(player1, List.of(new RooftopPercher()));
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
    }

    @Test
    @DisplayName("Ability fizzles when all targeted cards are removed from graveyards (no life gain)")
    void allTargetsRemovedCausesFizzle() {
        setupGraveyards();
        setupAndCast();
        GameData gd = harness.getGameData();
        harness.passBothPriorities();
        List<UUID> targets = new ArrayList<>(gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .subList(0, 2);
        harness.handleMultipleCardsChosen(player1, targets);

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("fizzles"));
    }

    @Test
    @DisplayName("Selecting too many cards throws exception")
    void tooManyCardsThrows() {
        // Three cards in graveyards, max is 2
        harness.setGraveyard(player1, List.of(new RooftopPercher(), new Plains(), new RooftopPercher()));
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        List<UUID> allIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(allIds).hasSize(3);

        // Try to select all 3 (max is 2)
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, allIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Selecting invalid card ID throws exception")
    void invalidCardIdThrows() {
        setupGraveyards();
        setupAndCast();

        harness.passBothPriorities(); // resolve creature → target prompt

        UUID fakeId = UUID.randomUUID();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(fakeId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
    }

    @Test
    @DisplayName("Wrong player choosing throws exception")
    void wrongPlayerThrows() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Targets may span both graveyards")
    void targetsMaySpanBothGraveyards() {
        Card ownCard = new RooftopPercher();
        Card opposingCard = new Plains();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        setupAndCast();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId(), opposingCard.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingCard);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("One remaining legal target is exiled and the full life gain applies")
    void oneTargetRemovedStillGainsThreeLife() {
        Card removedCard = new RooftopPercher();
        Card remainingCard = new Plains();
        harness.setGraveyard(player1, List.of(removedCard));
        harness.setGraveyard(player2, List.of(remainingCard));
        setupAndCast();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(removedCard.getId(), remainingCard.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 23);
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new RooftopPercher(), "{5}");
    }

    private void setupGraveyards() {
        harness.setGraveyard(player1, List.of(new RooftopPercher(), new Plains()));
        harness.setGraveyard(player2, List.of(new RooftopPercher()));
    }
}

