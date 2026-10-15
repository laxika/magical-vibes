package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.d.Divination;
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

@CardUsed({AdventurousImpulse.class, LlanowarElves.class, Plains.class, Divination.class})
class AdventurousImpulseTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Adventurous Impulse puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Adventurous Impulse");
    }

    @Test
    @DisplayName("Resolves by offering creature and land cards among top three")
    void resolvesOfferingCreaturesAndLands() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Divination(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Plains");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand then orders rest on bottom")
    void choosingCreatureThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Divination divination = new Divination();
        Plains plains = new Plains();
        setupTopCards(List.of(elves, divination, plains));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose Llanowar Elves
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a land puts it into hand then orders rest on bottom")
    void choosingLandThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Divination divination = new Divination();
        Plains plains = new Plains();
        setupTopCards(List.of(elves, divination, plains));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose Plains (index 1 since only Llanowar Elves and Plains are offered)
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("You may choose no card and still reorder all looked cards to bottom")
    void mayChooseNoCard() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Divination(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("If top three has no creature or land cards, directly reorder them to bottom")
    void noMatchingCardsDirectlyReordersBottom() {
        setupTopCards(List.of(
                new Divination(),
                new Divination(),
                new Divination()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("With empty library, Adventurous Impulse does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Adventurous Impulse goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Divination(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        harness.assertInGraveyard(player1, "Adventurous Impulse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With fewer than three cards in library, looks at all available")
    void fewerThanThreeCardsInLibrary() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Plains");
    }

    @Test
    void chosenCardIsRevealedAndRestGoesBelowUnseenCardsInChosenOrder() {
        LlanowarElves elves = new LlanowarElves();
        Divination divination = new Divination();
        Plains plains = new Plains();
        LlanowarElves unseen = new LlanowarElves();
        harness.setLibrary(player1, List.of(elves, divination, plains, unseen));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, plains, divination);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Llanowar Elves"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Adventurous Impulse");
    }

    @Test
    void mayDeclineOnlyCardInLibrary() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Adventurous Impulse");
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}

