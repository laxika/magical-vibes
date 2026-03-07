package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardsToGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LifesFinaleTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Life's Finale has destroy all creatures and library search effects")
    void hasCorrectEffects() {
        LifesFinale card = new LifesFinale();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DestroyAllPermanentsEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(SearchTargetLibraryForCardsToGraveyardEffect.class);
    }

    // ===== Board wipe =====

    @Test
    @DisplayName("Destroys all creatures on resolution")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both creatures should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Creatures should be in graveyards
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Library search =====

    @Test
    @DisplayName("After board wipe, presents library search for creature cards")
    void presentsLibrarySearchAfterBoardWipe() {
        setupOpponentLibraryWithCreatures();

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Only creature cards are shown in the search")
    void onlyCreatureCardsInSearch() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
                new GrizzlyBears(), new Peek(), new Swamp(), new GrizzlyBears()
        ));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only creature cards should appear in the search
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getType() == CardType.CREATURE);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Chosen creature card goes to opponent's graveyard")
    void chosenCardGoesToOpponentGraveyard() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose the creature card
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Card should be in opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));

        // Card should not be in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Can choose up to three creature cards sequentially")
    void canChooseUpToThreeCreatures() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears1, bears2, bears3, new Peek()));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Pick first creature
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Pick second creature
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Pick third creature
        gs.handleLibraryCardChosen(gd, player1, 0);

        // All three should be in opponent's graveyard
        long graveyardCreatures = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(graveyardCreatures).isEqualTo(3);

        // Library should only have the non-creature card left (Peek)
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can decline to find more cards (fail to find)")
    void canDeclineToFindMoreCards() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears1, bears2));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Pick first creature
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Decline to pick more (-1 = fail to find)
        gs.handleLibraryCardChosen(gd, player1, -1);

        // Only one creature should be in graveyard
        long graveyardCreatures = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(graveyardCreatures).isEqualTo(1);

        // Search should be complete
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("Library is shuffled after search completes")
    void libraryIsShuffledAfterSearch() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Decline to pick any card
        gs.handleLibraryCardChosen(gd, player1, -1);

        // Log should mention shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled") || log.contains("Library is shuffled"));
    }

    // ===== Empty / no creature library =====

    @Test
    @DisplayName("Empty opponent library skips search")
    void emptyLibrarySkipsSearch() {
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should not be awaiting library search
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("No creature cards in opponent library skips search")
    void noCreaturesInLibrarySkipsSearch() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Peek(), new Swamp()));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should not be awaiting library search
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Log should mention no matching cards
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no matching cards"));
    }

    // ===== Sorcery after resolution =====

    @Test
    @DisplayName("Life's Finale goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Life's Finale"));
    }

    // ===== Fewer than 3 creature cards =====

    @Test
    @DisplayName("Search ends when no more creature cards remain in library")
    void searchEndsWhenNoMoreCreatures() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, new Peek()));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Pick the only creature
        gs.handleLibraryCardChosen(gd, player1, 0);

        // No more creature cards — search should end automatically
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // The creature should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    // ===== Helpers =====

    private void setupOpponentLibraryWithCreatures() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));
    }
}
