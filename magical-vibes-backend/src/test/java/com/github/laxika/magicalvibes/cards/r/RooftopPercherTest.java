package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RooftopPercherTest {

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
    @DisplayName("Rooftop Percher has correct card properties")
    void hasCorrectProperties() {
        RooftopPercher card = new RooftopPercher();

        assertThat(card.getName()).isEqualTo("Rooftop Percher");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{5}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(card.getKeywords()).isEqualTo(Set.of(Keyword.CHANGELING, Keyword.FLYING));
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ExileCardsFromGraveyardEffect.class);
        ExileCardsFromGraveyardEffect effect = (ExileCardsFromGraveyardEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.maxTargets()).isEqualTo(2);
        assertThat(effect.lifeGain()).isEqualTo(3);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Rooftop Percher puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new RooftopPercher()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Rooftop Percher");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== ETB: target selection at trigger time =====

    @Test
    @DisplayName("Resolving creature prompts for graveyard target selection before ability goes on stack")
    void resolvingCreaturePromptsTargetSelection() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → ETB triggers → target selection prompt

        // Rooftop Percher is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rooftop Percher"));

        // Graveyard target selection is pending (at trigger time, before ability goes on stack)
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.awaitingMultiGraveyardChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.awaitingMultiGraveyardChoiceMaxCount).isEqualTo(2);

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
        List<UUID> validIds = new ArrayList<>(gd.awaitingMultiGraveyardChoiceValidCardIds);
        List<UUID> chosenIds = validIds.subList(0, 2);

        int totalGraveyardBefore = gd.playerGraveyards.get(player1.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size();

        // Select targets → ability goes on stack
        harness.handleMultipleGraveyardCardsChosen(player1, chosenIds);
        harness.passBothPriorities(); // resolve ETB → exile + life gain

        // Total graveyard cards reduced by 2
        int totalGraveyardAfter = gd.playerGraveyards.get(player1.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size();
        assertThat(totalGraveyardAfter).isEqualTo(totalGraveyardBefore - 2);

        // Total exiled cards increased by 2
        int totalExiled = gd.playerExiledCards.get(player1.getId()).size()
                + gd.playerExiledCards.get(player2.getId()).size();
        assertThat(totalExiled).isEqualTo(2);

        // Life was gained on resolution
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);

        // Awaiting state is cleared
        assertThat(gd.awaitingInput).isNull();

        // Log mentions exile
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("exiles") && entry.contains("from graveyard"));
    }

    @Test
    @DisplayName("Can exile cards from opponent's graveyard")
    void canExileFromOpponentGraveyard() {
        // Only opponent has graveyard cards
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Plains()));
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);

        // All valid IDs should be from player2's graveyard
        List<UUID> p2GraveyardIds = gd.playerGraveyards.get(player2.getId()).stream()
                .map(Card::getId).toList();
        assertThat(gd.awaitingMultiGraveyardChoiceValidCardIds).containsAll(p2GraveyardIds);

        // Exile both → ability goes on stack
        harness.handleMultipleGraveyardCardsChosen(player1, p2GraveyardIds);
        harness.passBothPriorities(); // resolve ETB → exile + life gain

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Can exile one card when choosing fewer than maximum")
    void canExileFewerThanMax() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        List<UUID> validIds = new ArrayList<>(gd.awaitingMultiGraveyardChoiceValidCardIds);

        // Choose only one card → ability goes on stack
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(validIds.getFirst()));
        harness.passBothPriorities(); // resolve ETB → exile + life gain

        int totalExiled = gd.playerExiledCards.get(player1.getId()).size()
                + gd.playerExiledCards.get(player2.getId()).size();
        assertThat(totalExiled).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.awaitingInput).isNull();
    }

    // ===== ETB: choosing zero targets =====

    @Test
    @DisplayName("Choosing zero targets gains life but exiles nothing")
    void choosingZeroTargetsGainsLifeOnly() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        // Choose 0 targets (allowed by "up to") → ability goes on stack with no targets
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());
        harness.passBothPriorities(); // resolve ETB → life gain only

        // No cards exiled
        int totalExiled = gd.playerExiledCards.get(player1.getId()).size()
                + gd.playerExiledCards.get(player2.getId()).size();
        assertThat(totalExiled).isEqualTo(0);

        // Life was still gained (ability resolves normally with 0 targets)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.awaitingInput).isNull();
    }

    // ===== ETB with empty graveyards =====

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
        assertThat(gd.awaitingInput).isNull();
    }

    // ===== Max count capping =====

    @Test
    @DisplayName("Max count is capped to available cards when fewer than 2 in graveyards")
    void maxCountCappedToAvailableCards() {
        // Only one card in graveyards total
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.awaitingMultiGraveyardChoiceMaxCount).isEqualTo(1);
        assertThat(gd.awaitingMultiGraveyardChoiceValidCardIds).hasSize(1);
    }

    // ===== Fizzle: all targets removed before resolution =====

    @Test
    @DisplayName("Ability fizzles when all targeted cards are removed from graveyards (no life gain)")
    void allTargetsRemovedCausesFizzle() {
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Create cards whose IDs we'll target, but they won't be in graveyards
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();

        // Put Rooftop Percher on the battlefield (it already entered)
        harness.addToBattlefield(player1, new RooftopPercher());

        // Manually place a triggered ability on the stack with targets pointing to cards NOT in any graveyard
        List<ExileCardsFromGraveyardEffect> effects = List.of(new ExileCardsFromGraveyardEffect(2, 3));
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                new RooftopPercher(),
                player1.getId(),
                "Rooftop Percher's ETB ability",
                new ArrayList<>(effects),
                List.of(bears1.getId(), bears2.getId())
        ));

        // Resolve the ability — all targets are invalid → fizzle
        harness.passBothPriorities();

        // Life was NOT gained (ability fizzled)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        // Log mentions fizzle
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("fizzles"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Selecting too many cards throws exception")
    void tooManyCardsThrows() {
        // Three cards in graveyards, max is 2
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new GrizzlyBears()));
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        List<UUID> allIds = new ArrayList<>(gd.awaitingMultiGraveyardChoiceValidCardIds);
        assertThat(allIds).hasSize(3);

        // Try to select all 3 (max is 2)
        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player1, allIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Selecting invalid card ID throws exception")
    void invalidCardIdThrows() {
        setupGraveyards();
        setupAndCast();

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve creature → target prompt

        UUID fakeId = UUID.randomUUID();
        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player1, List.of(fakeId)))
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

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);

        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    // ===== Changeling keyword =====

    @Test
    @DisplayName("Rooftop Percher has Changeling and counts as every creature type")
    void hasChangelingKeyword() {
        harness.addToBattlefield(player1, new RooftopPercher());

        GameData gd = harness.getGameData();
        Permanent percher = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rooftop Percher"))
                .findFirst()
                .orElseThrow();

        assertThat(percher.hasKeyword(Keyword.CHANGELING)).isTrue();
        assertThat(percher.hasKeyword(Keyword.FLYING)).isTrue();
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RooftopPercher()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
    }

    private void setupGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
    }
}
