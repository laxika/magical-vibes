package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscombobulateTest {

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
    @DisplayName("Discombobulate has correct card properties")
    void hasCorrectProperties() {
        Discombobulate card = new Discombobulate();

        assertThat(card.getName()).isEqualTo("Discombobulate");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{2}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CounterSpellEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ReorderTopCardsOfLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Discombobulate puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        UUID bearsCardId = bears.getId();
        harness.castInstant(player2, 0, bearsCardId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry discombobulateEntry = gd.stack.getLast();
        assertThat(discombobulateEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(discombobulateEntry.getCard().getName()).isEqualTo("Discombobulate");
        assertThat(discombobulateEntry.getTargetPermanentId()).isEqualTo(bearsCardId);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters target spell and puts it in owner's graveyard")
    void resolvingCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Resolving enters library reorder state for caster")
    void resolvingEntersLibraryReorderState() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(4);
    }

    @Test
    @DisplayName("Discombobulate goes to caster's graveyard after resolving")
    void discombobulateGoesToGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Discombobulate"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Library reorder =====

    @Test
    @DisplayName("Library reorder changes top cards of library")
    void libraryReorderChangesTopCards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        Card originalTop3 = deck.get(3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        // Reverse the top 4 cards
        harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(3, 2, 1, 0));

        assertThat(deck.get(0)).isSameAs(originalTop3);
        assertThat(deck.get(1)).isSameAs(originalTop2);
        assertThat(deck.get(2)).isSameAs(originalTop1);
        assertThat(deck.get(3)).isSameAs(originalTop0);
    }

    @Test
    @DisplayName("Library reorder clears awaiting state")
    void libraryReorderClearsAwaitingState() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(0, 1, 2, 3));

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.awaitingLibraryReorderPlayerId()).isNull();
        assertThat(gd.interaction.awaitingLibraryReorderCards()).isNull();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles entirely if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        // Remove Bears from stack before Discombobulate resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Entire spell fizzles — no counter, no library reorder
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.awaitingInputType()).isNull();
        // Discombobulate still goes to graveyard when fizzling
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Discombobulate"));
    }

    // ===== Library edge cases =====

    @Test
    @DisplayName("Library with fewer than 4 cards reorders available cards")
    void libraryWithFewerThanFourCards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        Card cardA = new GrizzlyBears();
        Card cardB = new GrizzlyBears();
        deck.add(cardA);
        deck.add(cardB);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(2);

        // Swap the 2 cards
        harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(1, 0));

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }

    @Test
    @DisplayName("Library with exactly 1 card skips reorder prompt")
    void libraryWithOneCardSkipsReorder() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("Empty library skips reorder entirely")
    void emptyLibrarySkipsReorder() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).clear();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Library reorder validation =====

    @Test
    @DisplayName("Reorder rejects wrong number of indices")
    void reorderRejectsWrongCount() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(0, 1))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must specify order for all");
    }

    @Test
    @DisplayName("Reorder rejects duplicate indices")
    void reorderRejectsDuplicateIndices() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(0, 0, 1, 2))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate card index");
    }

    @Test
    @DisplayName("Reorder rejects wrong player")
    void reorderRejectsWrongPlayer() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2, 3))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to reorder");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records counter and reorder actions")
    void gameLogRecordsActions() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Discombobulate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("countered"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top") && log.contains("cards"));

        // Complete the reorder
        harness.getGameService().handleLibraryCardsReordered(gd, player2, List.of(0, 1, 2, 3));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("puts") && log.contains("cards back on top"));
    }
}


