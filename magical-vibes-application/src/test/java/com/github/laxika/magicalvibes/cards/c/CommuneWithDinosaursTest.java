package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.f.FirecannonBlast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommuneWithDinosaurs.class, ChargingMonstrosaur.class, FirecannonBlast.class, Plains.class, Forest.class})
class CommuneWithDinosaursTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Commune with Dinosaurs puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(CommuneWithDinosaurs.class);
    }

    @Test
    @DisplayName("Resolves by offering only Dinosaur and land cards among top five")
    void resolvesOfferingDinosaursAndLands() {
        setupTopFive(List.of(
                new ChargingMonstrosaur(),
                new FirecannonBlast(),
                new FirecannonBlast(),
                new Plains(),
                new Forest()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Charging Monstrosaur", "Plains", "Forest");
    }

    @Test
    @DisplayName("Choosing a Dinosaur puts it into hand then orders rest on bottom")
    void choosingDinosaurThenOrderingBottom() {
        ChargingMonstrosaur dino = new ChargingMonstrosaur();
        FirecannonBlast blast1 = new FirecannonBlast();
        FirecannonBlast blast2 = new FirecannonBlast();
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupTopFive(List.of(dino, blast1, blast2, plains, forest));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose Charging Monstrosaur
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Charging Monstrosaur");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Choosing a land puts it into hand")
    void choosingLandPutsIntoHand() {
        ChargingMonstrosaur dino = new ChargingMonstrosaur();
        FirecannonBlast blast1 = new FirecannonBlast();
        FirecannonBlast blast2 = new FirecannonBlast();
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupTopFive(List.of(dino, blast1, blast2, plains, forest));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // The eligible cards are: Charging Monstrosaur (0), Plains (1), Forest (2)
        // Choose Plains (index 1)
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("May choose no card and still reorder all looked cards to bottom")
    void mayChooseNothing() {
        setupTopFive(List.of(
                new ChargingMonstrosaur(),
                new FirecannonBlast(),
                new FirecannonBlast(),
                new Plains(),
                new Forest()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no Dinosaurs or lands, directly reorder to bottom")
    void noMatchesDirectlyReordersBottom() {
        setupTopFive(List.of(
                new FirecannonBlast(),
                new FirecannonBlast(),
                new FirecannonBlast(),
                new FirecannonBlast(),
                new FirecannonBlast()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("Commune with Dinosaurs goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopFive(List.of(
                new ChargingMonstrosaur(),
                new FirecannonBlast(),
                new FirecannonBlast(),
                new Plains(),
                new Forest()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        harness.assertInGraveyard(player1, "Commune with Dinosaurs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the top five are inspected and the remainder stays above the ordered bottom cards")
    void preservesUnlookedCardsAndChosenBottomOrder() {
        ChargingMonstrosaur dinosaur = new ChargingMonstrosaur();
        FirecannonBlast first = new FirecannonBlast();
        FirecannonBlast second = new FirecannonBlast();
        Plains plains = new Plains();
        Forest forest = new Forest();
        ChargingMonstrosaur sixth = new ChargingMonstrosaur();
        Forest seventh = new Forest();
        harness.setLibrary(player1, List.of(dinosaur, first, second, plains, forest, sixth, seventh));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(dinosaur, plains, forest);
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dinosaur);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(sixth, seventh, forest, second, first, plains);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A single eligible card in a short library can still be declined")
    void mayDeclineOnlyEligibleCardInShortLibrary() {
        Forest forest = new Forest();
        FirecannonBlast other = new FirecannonBlast();
        harness.setLibrary(player1, List.of(forest, other));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);
        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other, forest);
        harness.assertInGraveyard(player1, "Commune with Dinosaurs");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing the only card in the library completes resolution without a reorder")
    void choosesOnlyLibraryCard() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Commune with Dinosaurs");
    }

    @Test
    @DisplayName("An empty library resolves without a choice or drawing a card")
    void emptyLibraryResolves() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Commune with Dinosaurs");
    }
    private void setupTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
