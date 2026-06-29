package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuguryOwlTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Augury Owl has scry 3 ETB effect")
    void hasCorrectProperties() {
        AuguryOwl card = new AuguryOwl();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ScryEffect.class);
        ScryEffect effect = (ScryEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Augury Owl puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Augury Owl");
    }

    @Test
    @DisplayName("Resolving Augury Owl enters battlefield and triggers ETB scry")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Augury Owl"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Augury Owl");
    }

    @Test
    @DisplayName("Resolving ETB enters scry state")
    void resolvingEtbEntersScryState() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(3);
    }

    // ===== Scry puts cards on top =====

    @Test
    @DisplayName("Scry keeping all cards on top preserves them in order")
    void scryAllOnTop() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Keep all on top in original order
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0, 1, 2), List.of());

        assertThat(deck.get(0)).isSameAs(originalTop0);
        assertThat(deck.get(1)).isSameAs(originalTop1);
        assertThat(deck.get(2)).isSameAs(originalTop2);
    }

    @Test
    @DisplayName("Scry reordering cards on top changes library order")
    void scryReorderOnTop() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Reverse order on top
        harness.getGameService().handleScryCompleted(gd, player1, List.of(2, 1, 0), List.of());

        assertThat(deck.get(0)).isSameAs(originalTop2);
        assertThat(deck.get(1)).isSameAs(originalTop1);
        assertThat(deck.get(2)).isSameAs(originalTop0);
    }

    // ===== Scry puts cards on bottom =====

    @Test
    @DisplayName("Scry putting all cards on bottom moves them to bottom of library")
    void scryAllOnBottom() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        int originalDeckSize = deck.size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Put all on bottom
        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0, 1, 2));

        // Top of library should no longer be the scried cards
        assertThat(deck.get(0)).isNotSameAs(originalTop0);

        // Cards should be at the bottom
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 3)).isSameAs(originalTop0);
        assertThat(deck.get(deckSize - 2)).isSameAs(originalTop1);
        assertThat(deck.get(deckSize - 1)).isSameAs(originalTop2);
    }

    // ===== Scry split (some top, some bottom) =====

    @Test
    @DisplayName("Scry putting one on top and two on bottom splits correctly")
    void scrySplitTopAndBottom() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Keep card 1 on top, put cards 0 and 2 on bottom
        harness.getGameService().handleScryCompleted(gd, player1, List.of(1), List.of(0, 2));

        // Top of library should be card 1
        assertThat(deck.get(0)).isSameAs(originalTop1);

        // Bottom of library should have cards 0 and 2
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 2)).isSameAs(originalTop0);
        assertThat(deck.get(deckSize - 1)).isSameAs(originalTop2);
    }

    // ===== Scry clears state =====

    @Test
    @DisplayName("Completing scry clears awaiting state")
    void scryCompletionClearsState() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0, 1, 2), List.of());

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.scryContext()).isNull();
    }

    // ===== Library edge cases =====

    @Test
    @DisplayName("Library with fewer than 3 cards scries available cards")
    void libraryWithFewerThanThreeCards() {
        harness.setHand(player1, List.of(new AuguryOwl()));
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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext().cards()).hasSize(2);

        harness.getGameService().handleScryCompleted(gd, player1, List.of(1, 0), List.of());

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }

    @Test
    @DisplayName("Library with exactly 1 card enters scry state with 1 card")
    void libraryWithOneCard() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(singleCard);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);

        // Player can keep on top or put on bottom
        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0));

        int deckSize = gd.playerDecks.get(player1.getId()).size();
        assertThat(gd.playerDecks.get(player1.getId()).get(deckSize - 1)).isSameAs(singleCard);
    }

    @Test
    @DisplayName("Empty library scry event occurs but nothing to interact with")
    void emptyLibraryScryEventOccurs() {
        harness.setHand(player1, List.of(new AuguryOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }
}
