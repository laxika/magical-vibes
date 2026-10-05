package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NestedShambler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfaneTutor.class, NestedShambler.class, PithingNeedle.class})
class ProfaneTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Profane Tutor with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        ProfaneTutor card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that searches any card into hand")
    void lastCounterOffersFreeCastAndSearchesAnyCardIntoHand() {
        ProfaneTutor card = suspendCard();
        NestedShambler chosenCard = new NestedShambler();
        harness.setLibrary(player1, List.of(chosenCard, new NestedShambler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyElementsOf(gd.playerDecks.get(player1.getId()));
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().reveals()).isFalse();

        harness.handleCardChosen(player1, search.params().cards().indexOf(chosenCard));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosenCard).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Suspend is a special action unaffected by Pithing Needle")
    void pithingNeedleDoesNotPreventSuspending() {
        harness.castFromHand(player1, new PithingNeedle(), "{1}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Profane Tutor");

        ProfaneTutor card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    @DisplayName("Only the owner's upkeep removes a counter, and removal uses the stack")
    void countersAreRemovedByOwnersUpkeepTrigger() {
        ProfaneTutor card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the card exiled without future upkeep triggers")
    void decliningCastLeavesCardExiled() {
        ProfaneTutor card = suspendCard();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Searching an empty library completes without a card choice")
    void emptyLibrarySearchCompletes() {
        ProfaneTutor card = suspendCard();
        harness.setLibrary(player1, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private ProfaneTutor suspendCard() {
        ProfaneTutor card = new ProfaneTutor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
