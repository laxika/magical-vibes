package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SageOwlTest {

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
    @DisplayName("Sage Owl has correct card properties")
    void hasCorrectProperties() {
        SageOwl card = new SageOwl();

        assertThat(card.getName()).isEqualTo("Sage Owl");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ReorderTopCardsOfLibraryEffect.class);
        ReorderTopCardsOfLibraryEffect effect = (ReorderTopCardsOfLibraryEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(4);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sage Owl puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sage Owl");
    }

    @Test
    @DisplayName("Resolving Sage Owl enters battlefield and triggers ETB reorder")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sage Owl"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Sage Owl");
    }

    @Test
    @DisplayName("Resolving ETB enters library reorder state")
    void resolvingEtbEntersLibraryReorderState() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingLibraryReorderCards).hasSize(4);
    }

    @Test
    @DisplayName("Library reorder changes top cards of library")
    void libraryReorderChangesTopCards() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        Card originalTop3 = deck.get(3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Reverse the top 4 cards
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(3, 2, 1, 0));

        assertThat(deck.get(0)).isSameAs(originalTop3);
        assertThat(deck.get(1)).isSameAs(originalTop2);
        assertThat(deck.get(2)).isSameAs(originalTop1);
        assertThat(deck.get(3)).isSameAs(originalTop0);
    }

    @Test
    @DisplayName("Library reorder clears awaiting state")
    void libraryReorderClearsAwaitingState() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2, 3));

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId).isNull();
        assertThat(gd.interaction.awaitingLibraryReorderCards).isNull();
    }

    // ===== Library edge cases =====

    @Test
    @DisplayName("Library with fewer than 4 cards reorders available cards")
    void libraryWithFewerThanFourCards() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card cardA = new GrizzlyBears();
        Card cardB = new GrizzlyBears();
        deck.add(cardA);
        deck.add(cardB);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards).hasSize(2);

        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(1, 0));

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }

    @Test
    @DisplayName("Library with exactly 1 card skips reorder prompt")
    void libraryWithOneCardSkipsReorder() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("Empty library skips reorder entirely")
    void emptyLibrarySkipsReorder() {
        harness.setHand(player1, List.of(new SageOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Sage Owl has flying =====

    @Test
    @DisplayName("Sage Owl on the battlefield has flying")
    void sageOwlHasFlying() {
        harness.addToBattlefield(player1, new SageOwl());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sage Owl")
                        && p.getCard().getKeywords().contains(Keyword.FLYING));
    }
}

