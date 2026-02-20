package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MidnightRitualTest {

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
    @DisplayName("Midnight Ritual has correct card properties")
    void hasCorrectProperties() {
        MidnightRitual card = new MidnightRitual();

        assertThat(card.getName()).isEqualTo("Midnight Ritual");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{X}{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExileCreaturesFromGraveyardAndCreateTokensEffect.class);
    }

    // ===== Casting with X > 0 enters graveyard choice =====

    @Test
    @DisplayName("Casting with X=2 prompts for graveyard target selection")
    void castingWithXPromptsGraveyardChoice() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5); // {X}{2}{B} with X=2 costs 5

        harness.castSorcery(player1, 0, 2);

        // Should be awaiting multi-graveyard choice
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.awaitingMultiGraveyardChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceMaxCount).isEqualTo(2);

        // Spell is NOT yet on the stack (targets must be chosen first)
        assertThat(gd.stack).isEmpty();

        // Card was removed from hand and mana was paid
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only creature cards from controller's graveyard are valid targets")
    void onlyCreatureCardsAreValidTargets() {
        Card bears = new GrizzlyBears();
        Card plains = new Plains(); // not a creature
        Card opponentBears = new GrizzlyBears(); // opponent's creature
        harness.setGraveyard(player1, List.of(bears, plains));
        harness.setGraveyard(player2, List.of(opponentBears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=1, costs 4

        harness.castSorcery(player1, 0, 1);

        // Only player1's creature card should be valid (not Plains, not opponent's card)
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds).containsExactly(bears.getId());
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds).doesNotContain(plains.getId());
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds).doesNotContain(opponentBears.getId());
    }

    // ===== Target selection puts spell on stack =====

    @Test
    @DisplayName("Selecting targets puts sorcery on the stack")
    void selectingTargetsPutsSpellOnStack() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        // Spell is now on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Midnight Ritual");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetCardIds()).containsExactly(bears1.getId(), bears2.getId());

        // Awaiting state is cleared
        assertThat(gd.interaction.awaitingInput).isNull();

        // Log mentions casting
        assertThat(gd.gameLog).anyMatch(l -> l.contains("casts Midnight Ritual"));
    }

    // ===== Resolution: exile + token creation =====

    @Test
    @DisplayName("Resolving exiles targeted creatures and creates Zombie tokens")
    void resolvingExilesAndCreatesTokens() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));
        harness.passBothPriorities(); // resolve the spell

        // Both creatures exiled from graveyard; only Midnight Ritual remains (sorcery goes to graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Midnight Ritual");
        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .extracting(Card::getName)
                .containsOnly("Grizzly Bears");

        // Two Zombie tokens on the battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(2);

        // Stack is empty after resolution
        assertThat(gd.stack).isEmpty();

        // Log mentions exile
        assertThat(gd.gameLog).anyMatch(l -> l.contains("exiles") && l.contains("from graveyard"));
    }

    @Test
    @DisplayName("Zombie tokens have correct properties")
    void zombieTokensHaveCorrectProperties() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=1

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    // ===== X = 0 =====

    @Test
    @DisplayName("Casting with X=0 puts spell on stack directly without graveyard prompt")
    void castingWithXZeroPutsOnStackDirectly() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {X}{2}{B} with X=0 costs 3

        harness.castSorcery(player1, 0, 0);

        // No graveyard choice — spell goes directly on stack
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getTargetCardIds()).isEmpty();
    }

    @Test
    @DisplayName("Resolving with X=0 does nothing")
    void resolvingWithXZeroDoesNothing() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // No creatures exiled, no tokens created; graveyard has bears + Midnight Ritual
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    // ===== X > creature count in graveyard =====

    @Test
    @DisplayName("Casting with X greater than creature count in graveyard throws error")
    void xGreaterThanCreatureCountThrows() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears())); // only 1 creature
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=2

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature cards in graveyard");
    }

    @Test
    @DisplayName("Non-creature cards in graveyard do not count toward X validation")
    void nonCreaturesDontCountForXValidation() {
        // Graveyard has 1 creature and 2 non-creatures
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Plains()));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        // X=2 should fail because only 1 creature is available
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature cards in graveyard");
    }

    // ===== Empty graveyard =====

    @Test
    @DisplayName("Casting with X=1 and empty graveyard throws error")
    void emptyGraveyardWithXOneThrows() {
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature cards in graveyard");
    }

    @Test
    @DisplayName("Casting with X=0 and empty graveyard is legal")
    void emptyGraveyardWithXZeroIsLegal() {
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.awaitingInput).isNull();
    }

    // ===== Multiple tokens =====

    @Test
    @DisplayName("X=3 exiles three creatures and creates three Zombie tokens")
    void xThreeCreatesThreeTokens() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card angel = new SerraAngel();
        harness.setGraveyard(player1, List.of(bears1, bears2, angel));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=3, costs 6

        harness.castSorcery(player1, 0, 3);
        List<UUID> allIds = new ArrayList<>(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds);
        assertThat(allIds).hasSize(3);

        harness.handleMultipleGraveyardCardsChosen(player1, allIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Midnight Ritual");
        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(3);

        long zombieCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .count();
        assertThat(zombieCount).isEqualTo(3);
    }

    // ===== Target removed before resolution =====

    @Test
    @DisplayName("Target removed from graveyard before resolution creates fewer tokens")
    void targetRemovedBeforeResolutionCreatesFewerTokens() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        // Remove one target from graveyard before resolution (simulating opponent interaction)
        gd.playerGraveyards.get(player1.getId()).removeIf(c -> c.getId().equals(bears1.getId()));

        harness.passBothPriorities();

        // Only one creature was actually exiled
        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(1);

        // Only one Zombie token created
        long zombieCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .count();
        assertThat(zombieCount).isEqualTo(1);
    }

    @Test
    @DisplayName("All targets removed before resolution creates no tokens")
    void allTargetsRemovedCreatesNoTokens() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        // Remove all targets from graveyard before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // No creatures exiled, no tokens
        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Selecting more cards than X throws error")
    void selectingMoreThanXThrows() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2, bears3));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=2

        harness.castSorcery(player1, 0, 2);

        // Try to select 3 cards when max is 2
        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player1,
                List.of(bears1.getId(), bears2.getId(), bears3.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Selecting invalid card ID throws error")
    void selectingInvalidCardIdThrows() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);

        UUID fakeId = UUID.randomUUID();
        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player1, List.of(fakeId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
    }

    @Test
    @DisplayName("Selecting duplicate card IDs throws error")
    void selectingDuplicateIdsThrows() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);

        // Try to select the same card twice
        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player1,
                List.of(bears1.getId(), bears1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    @DisplayName("Wrong player choosing throws error")
    void wrongPlayerChoosingThrows() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    // ===== Choosing fewer than X is not allowed =====

    @Test
    @DisplayName("Choosing fewer cards than X throws error (X target, not up to X)")
    void choosingFewerThanXThrowsError() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=2

        harness.castSorcery(player1, 0, 2);

        // Choosing only 1 card when X=2 is not allowed — card says "X target" not "up to X target"
        assertThatThrownBy(() -> harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose exactly 2 targets");
    }

    // ===== Graveyard with mixed card types =====

    @Test
    @DisplayName("Graveyard with mixed types only shows creatures as valid targets")
    void mixedGraveyardOnlyCreaturesValid() {
        Card bears = new GrizzlyBears();
        Card angel = new SerraAngel();
        Card plains1 = new Plains();
        Card plains2 = new Plains();
        harness.setGraveyard(player1, List.of(bears, plains1, angel, plains2));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=2

        harness.castSorcery(player1, 0, 2);

        // Only the 2 creatures should be valid
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds).hasSize(2);
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds).contains(bears.getId(), angel.getId());
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds).doesNotContain(plains1.getId(), plains2.getId());
    }

    // ===== Sorcery goes to graveyard after resolution =====

    @Test
    @DisplayName("Midnight Ritual goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MidnightRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        // Midnight Ritual should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Midnight Ritual"));
    }
}

