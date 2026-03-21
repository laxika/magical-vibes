package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MirrorOfFateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorOfFateTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Mirror of Fate has correct ability structure")
    void hasCorrectAbilityStructure() {
        MirrorOfFate card = new MirrorOfFate();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof SacrificeSelfCost)
                .anyMatch(e -> e instanceof MirrorOfFateEffect);
    }

    // ===== No exiled cards: library gets exiled entirely =====

    @Test
    @DisplayName("With no exiled cards, entire library is exiled and library is empty")
    void noExiledCardsExilesEntireLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());

        // Set up a library with some cards
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(bears);
        gd.playerDecks.get(player1.getId()).add(elves);
        harness.setHand(player1, List.of());

        // Ensure no exiled cards (already empty by default)

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Library should be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        // Exiled cards should include the library cards
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Mirror of Fate should be sacrificed
        harness.assertNotOnBattlefield(player1, "Mirror of Fate");
        harness.assertInGraveyard(player1, "Mirror of Fate");
    }

    // ===== With exiled cards: player chooses which to put on top =====

    @Test
    @DisplayName("With exiled cards, player is prompted to choose up to 7")
    void withExiledCardsPromptsChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Put a card in exile
        Card exiledBears = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledBears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting mirror of fate choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MIRROR_OF_FATE_CHOICE);
    }

    @Test
    @DisplayName("Choosing a single exiled card puts it on top without reorder step")
    void choosingSingleCardPutsOnTopDirectly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Set up library
        Card libraryCard = new Shock();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(libraryCard);

        // Put one card in exile
        Card exiledBears = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledBears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the single card
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(exiledBears.getId()));

        // No reorder step needed for single card — library should have the card on top
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(1);
        assertThat(library.getFirst().getName()).isEqualTo("Grizzly Bears");

        // The original library card should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));

        // Chosen card should no longer be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(exiledBears.getId()));
    }

    @Test
    @DisplayName("Choosing multiple exiled cards triggers reorder step for ordering")
    void choosingMultipleCardsTriggersReorder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Set up library with a card
        Card libraryCard = new Shock();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(libraryCard);

        // Put cards in exile
        Card exiledBears = new GrizzlyBears();
        Card exiledElves = new LlanowarElves();
        gd.addToExile(player1.getId(), exiledBears);
        gd.addToExile(player1.getId(), exiledElves);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose both exiled cards
        harness.handleMultipleGraveyardCardsChosen(player1,
                List.of(exiledBears.getId(), exiledElves.getId()));

        // Should be awaiting library reorder (player chooses the order)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);

        // Cards should be removed from exile already
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(exiledBears.getId()))
                .noneMatch(c -> c.getId().equals(exiledElves.getId()));

        // The original library card should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));

        // Complete reorder: put Elves on top, Bears second (order: [1, 0])
        gs.handleLibraryCardsReordered(gd, player1, List.of(1, 0));

        // Library should now have Elves on top, Bears second
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(2);
        assertThat(library.get(0).getName()).isEqualTo("Llanowar Elves");
        assertThat(library.get(1).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Player can choose zero exiled cards")
    void canChooseZeroCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        Card libraryCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(libraryCard);

        Card exiledCard = new Shock();
        gd.addToExile(player1.getId(), exiledCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose nothing
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        // Library should be empty (all was exiled, nothing chosen)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        // All cards should be in exile (original library card + original exiled card)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    // ===== Mirror of Fate is sacrificed =====

    @Test
    @DisplayName("Mirror of Fate goes to graveyard after activation (sacrifice cost)")
    void sacrificeGoesToGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Exile zone is already empty by default

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mirror of Fate");
        harness.assertInGraveyard(player1, "Mirror of Fate");
    }

    // ===== Max 7 cards =====

    @Test
    @DisplayName("Cannot choose more than 7 exiled cards even if more exist")
    void maxSevenCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();

        // Put 10 cards in exile
        List<Card> exiledCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Card c = new GrizzlyBears();
            gd.addToExile(player1.getId(), c);
            exiledCards.add(c);
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MIRROR_OF_FATE_CHOICE);

        // Choose 7 of the 10
        List<UUID> chosen = exiledCards.stream().limit(7).map(Card::getId).toList();
        harness.handleMultipleGraveyardCardsChosen(player1, chosen);

        // Should be awaiting library reorder for the 7 cards
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);

        // Complete reorder: keep original order [0, 1, 2, 3, 4, 5, 6]
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2, 3, 4, 5, 6));

        // Library should have 7 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);

        // Remaining 3 should still be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    // ===== Does not affect opponent's exile or library =====

    @Test
    @DisplayName("Mirror of Fate only affects controller's library and exile zone")
    void doesNotAffectOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Set up player2's library and exile
        Card opponentLibraryCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(opponentLibraryCard);

        Card opponentExiledCard = new Shock();
        gd.addToExile(player2.getId(), opponentExiledCard);

        // Player1 has no exiled cards (already empty by default)
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Opponent's library and exile should be unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }
}
