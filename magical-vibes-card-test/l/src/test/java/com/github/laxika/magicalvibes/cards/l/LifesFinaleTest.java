package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.m.MyrSuperion;
import com.github.laxika.magicalvibes.cards.g.GitaxianProbe;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifesFinale.class, MyrSuperion.class, GitaxianProbe.class, Swamp.class})
class LifesFinaleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures on resolution")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new MyrSuperion());
        harness.addToBattlefield(player2, new MyrSuperion());

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both creatures should be destroyed
        harness.assertNotOnBattlefield(player1, "Myr Superion");
        harness.assertNotOnBattlefield(player2, "Myr Superion");

        // Creatures should be in graveyards
        harness.assertInGraveyard(player1, "Myr Superion");
        harness.assertInGraveyard(player2, "Myr Superion");
    }

    @Test
    @DisplayName("After board wipe, presents library search for creature cards")
    void presentsLibrarySearchAfterBoardWipe() {
        setupOpponentLibraryWithCreatures();

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Only creature cards are shown in the search")
    void onlyCreatureCardsInSearch() {
        harness.setLibrary(player2, List.of(
                new MyrSuperion(), new GitaxianProbe(), new Swamp(), new MyrSuperion()
        ));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only creature cards should appear in the search
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Chosen creature card goes to opponent's graveyard")
    void chosenCardGoesToOpponentGraveyard() {
        Card bears = new MyrSuperion();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose the creature card
        harness.handleCardChosen(player1, 0);

        // Card should be in opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));

        // Card should not be in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Found creature lands only in the target's graveyard — not exile, not the caster's")
    void foundCreatureLandsOnlyInTheTargetsGraveyard() {
        Card bears = new MyrSuperion();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
        harness.assertNotOnBattlefield(player1, "Myr Superion");
    }

    @Test
    @DisplayName("Can choose up to three creature cards sequentially")
    void canChooseUpToThreeCreatures() {
        Card bears1 = new MyrSuperion();
        Card bears2 = new MyrSuperion();
        Card bears3 = new MyrSuperion();
        harness.setLibrary(player2, List.of(bears1, bears2, bears3, new GitaxianProbe()));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Pick first creature
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Pick second creature
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Pick third creature
        harness.handleCardChosen(player1, 0);

        // All three should be in opponent's graveyard
        long graveyardCreatures = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Myr Superion"))
                .count();
        assertThat(graveyardCreatures).isEqualTo(3);

        // Library should only have the non-creature card left (GitaxianProbe)
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Myr Superion"));
    }

    @Test
    @DisplayName("Can decline to find more cards (fail to find)")
    void canDeclineToFindMoreCards() {
        Card bears1 = new MyrSuperion();
        Card bears2 = new MyrSuperion();
        harness.setLibrary(player2, List.of(bears1, bears2));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Pick first creature
        harness.handleCardChosen(player1, 0);

        // Decline to pick more (-1 = fail to find)
        harness.handleCardChosen(player1, -1);

        // Only one creature should be in graveyard
        long graveyardCreatures = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Myr Superion"))
                .count();
        assertThat(graveyardCreatures).isEqualTo(1);

        // Search should be complete
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Library is shuffled after search completes")
    void libraryIsShuffledAfterSearch() {
        Card bears = new MyrSuperion();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Decline to pick any card
        harness.handleCardChosen(player1, -1);

        // Log should mention shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffled") || log.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Empty opponent library skips search")
    void emptyLibrarySkipsSearch() {
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should not be awaiting library search
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No creature cards in opponent library skips search")
    void noCreaturesInLibrarySkipsSearch() {
        harness.setLibrary(player2, List.of(new GitaxianProbe(), new Swamp()));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should not be awaiting library search
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();

        // Log should mention no matching cards
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no matching cards"));
    }

    @Test
    @DisplayName("Life's Finale goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Life's Finale");
    }

    @Test
    @DisplayName("Search ends when no more creature cards remain in library")
    void searchEndsWhenNoMoreCreatures() {
        Card bears = new MyrSuperion();
        harness.setLibrary(player2, List.of(bears, new GitaxianProbe()));

        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Pick the only creature
        harness.handleCardChosen(player1, 0);

        // No more creature cards — search should end automatically
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();

        // The creature should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot target the caster")
    void cannotTargetCaster() {
        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Board wipe precedes search and preserves lands")
    void boardWipePrecedesSearchAndPreservesLands() {
        harness.addToBattlefield(player1, new MyrSuperion());
        harness.addToBattlefield(player2, new MyrSuperion());
        harness.addToBattlefield(player2, new Swamp());
        harness.setLibrary(player2, List.of(new MyrSuperion()));
        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.assertInGraveyard(player1, "Myr Superion");
        harness.assertInGraveyard(player2, "Myr Superion");
        harness.assertOnBattlefield(player2, "Swamp");
        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("Stops after three cards even when a fourth creature remains")
    void stopsAfterThreeCards() {
        Card fourth = new MyrSuperion();
        harness.setLibrary(player2, List.of(new MyrSuperion(), new MyrSuperion(), new MyrSuperion(), fourth));
        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Selected creatures move to the graveyard together after selection completes")
    void selectedCreaturesMoveTogether() {
        Card first = new MyrSuperion();
        Card second = new MyrSuperion();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new LifesFinale()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(first, second);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
    }

    private void setupOpponentLibraryWithCreatures() {
        harness.setLibrary(player2, List.of(
                new MyrSuperion(), new MyrSuperion(), new MyrSuperion()
        ));
    }
}
