package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(entry.getCard().getName()).isEqualTo("Commune with Dinosaurs");
    }

    @Test
    @DisplayName("Resolves by offering only Dinosaur and land cards among top five")
    void resolvesOfferingDinosaursAndLands() {
        setupTopFive(List.of(
                new ChargingMonstrosaur(),
                new Shock(),
                new Shock(),
                new Plains(),
                new Forest()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupTopFive(List.of(dino, shock1, shock2, plains, forest));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Charging Monstrosaur
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Charging Monstrosaur"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Choosing a land puts it into hand")
    void choosingLandPutsIntoHand() {
        ChargingMonstrosaur dino = new ChargingMonstrosaur();
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupTopFive(List.of(dino, shock1, shock2, plains, forest));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // The eligible cards are: Charging Monstrosaur (0), Plains (1), Forest (2)
        // Choose Plains (index 1)
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("May choose no card and still reorder all looked cards to bottom")
    void mayChooseNothing() {
        setupTopFive(List.of(
                new ChargingMonstrosaur(),
                new Shock(),
                new Shock(),
                new Plains(),
                new Forest()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no Dinosaurs or lands, directly reorder to bottom")
    void noMatchesDirectlyReordersBottom() {
        setupTopFive(List.of(
                new Shock(),
                new Shock(),
                new Shock(),
                new Shock(),
                new Shock()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("Commune with Dinosaurs goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopFive(List.of(
                new ChargingMonstrosaur(),
                new Shock(),
                new Shock(),
                new Plains(),
                new Forest()
        ));
        harness.setHand(player1, List.of(new CommuneWithDinosaurs()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Commune with Dinosaurs"));
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopFive(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
