package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTheWeatherlightTest extends BaseCardTest {

    @Test
    @DisplayName("Board the Weatherlight has correct effect configuration")
    void hasCorrectEffect() {
        BoardTheWeatherlight card = new BoardTheWeatherlight();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isInstanceOf(CardIsHistoricPredicate.class);
    }

    @Test
    @DisplayName("Casting Board the Weatherlight puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Board the Weatherlight");
    }

    @Test
    @DisplayName("Resolves by offering legendary creature among top five")
    void resolvesOfferingLegendaryCreature() {
        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Arvad the Cursed");
    }

    @Test
    @DisplayName("Resolves by offering artifact among top five")
    void resolvesOfferingArtifact() {
        BottleGnomes artifact = new BottleGnomes();
        setupTopCards(List.of(
                new GrizzlyBears(),
                artifact,
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Bottle Gnomes");
    }

    @Test
    @DisplayName("Offers multiple historic cards when several are among top five")
    void offersMultipleHistoricCards() {
        setupTopCards(List.of(
                new ArvadTheCursed(),
                new BottleGnomes(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Arvad the Cursed", "Bottle Gnomes");
    }

    @Test
    @DisplayName("Choosing a historic card puts it into hand then orders rest on bottom")
    void choosingHistoricCardThenOrderingBottom() {
        ArvadTheCursed arvad = new ArvadTheCursed();
        setupTopCards(List.of(
                arvad,
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Arvad the Cursed"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(4);
    }

    @Test
    @DisplayName("You may choose no card and still reorder all looked cards to bottom")
    void mayChooseNoCard() {
        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no historic cards, directly reorder them to bottom")
    void noHistoricCardsDirectlyReordersBottom() {
        setupTopCards(List.of(
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("With empty library, Board the Weatherlight does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Board the Weatherlight goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Board the Weatherlight"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With fewer than five cards in library, looks at all available")
    void fewerThanFiveCardsInLibrary() {
        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Arvad the Cursed");
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
