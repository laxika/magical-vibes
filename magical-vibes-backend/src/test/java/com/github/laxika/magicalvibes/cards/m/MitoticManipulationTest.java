package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MitoticManipulationTest extends BaseCardTest {

    @Test
    @DisplayName("Mitotic Manipulation has correct effect structure")
    void hasCorrectProperties() {
        MitoticManipulation card = new MitoticManipulation();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect.class);
        LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect effect =
                (LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(7);
    }

    @Test
    @DisplayName("Resolves by offering only cards matching permanent names on battlefield")
    void resolvesOfferingOnlyMatchingCards() {
        // Put a Grizzly Bears on the battlefield
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Set top 7 of library: only the second Grizzly Bears matches a permanent name
        setupTopSeven(List.of(
                new LlanowarElves(),
                new GrizzlyBears(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingLibrarySearchCanFailToFind()).isTrue();
        // Only the Grizzly Bears should be offered
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(1);
        assertThat(gd.interaction.awaitingLibrarySearchCards().getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a matching card puts it onto the battlefield then orders rest on bottom")
    void choosingMatchingCardPutsOnBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        Shock shock2 = new Shock();
        Plains plains2 = new Plains();
        setupTopSeven(List.of(bears, elves, shock, plains, swamp, shock2, plains2));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose the Grizzly Bears
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Grizzly Bears should be on the battlefield
        long bearsCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(bearsCount).isEqualTo(2); // original + newly placed

        // Should be reordering the remaining 6 cards
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(6);
    }

    @Test
    @DisplayName("May choose not to put a card onto the battlefield")
    void mayDeclineToChoose() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupTopSeven(List.of(
                new GrizzlyBears(),
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // No new permanent should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        // All 7 cards should be reordered to bottom
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(7);
    }

    @Test
    @DisplayName("No matching cards means all are reordered to bottom")
    void noMatchingCardsReordersAllToBottom() {
        // Battlefield has only a Grizzly Bears, but top 7 has no Grizzly Bears
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupTopSeven(List.of(
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No matching cards — go directly to reorder
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(7);
    }

    @Test
    @DisplayName("Considers permanents on all players' battlefields")
    void considersAllPlayersBattlefields() {
        // Opponent has Llanowar Elves on battlefield, player1 has no permanents
        harness.addToBattlefield(player2, new LlanowarElves());

        setupTopSeven(List.of(
                new LlanowarElves(),
                new GrizzlyBears(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // Llanowar Elves matches the opponent's permanent
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(1);
        assertThat(gd.interaction.awaitingLibrarySearchCards().getFirst().getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("Multiple matching cards are all offered for selection")
    void multipleMatchingCardsAreAllOffered() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        setupTopSeven(List.of(
                new GrizzlyBears(),
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new GrizzlyBears(),
                new LlanowarElves()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // All 4 matching cards should be offered
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(4);
        assertThat(gd.interaction.awaitingLibrarySearchCards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears", "Llanowar Elves", "Llanowar Elves");
    }

    @Test
    @DisplayName("With empty library, Mitotic Manipulation does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Mitotic Manipulation goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupTopSeven(List.of(
                new GrizzlyBears(),
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mitotic Manipulation"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card then completing reorder puts rest on bottom of library")
    void fullFlowWithReorder() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        setupTopSeven(List.of(bears, elves, shock, new Plains(), new Swamp(), new Shock(), new Plains()));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose the Grizzly Bears
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Now reorder the remaining 6 cards
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        List<Card> remaining = gd.interaction.awaitingLibraryReorderCards();
        assertThat(remaining).hasSize(6);

        // Reorder in original order (0,1,2,3,4,5)
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2, 3, 4, 5));

        // Library should have 6 cards on bottom
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        // No more awaiting input
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    private void setupTopSeven(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
