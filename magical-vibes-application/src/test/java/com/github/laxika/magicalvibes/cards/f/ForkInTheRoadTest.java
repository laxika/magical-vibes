package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForkInTheRoadTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two basic lands, putting one in hand and one in the graveyard")
    void searchesForTwoBasicLands() {
        Card plains = new Plains();
        Card island = new Island();
        Card swamp = new Swamp();
        Card evolvingWilds = new EvolvingWilds();
        Card fork = castForkInTheRoad(List.of(evolvingWilds, plains, island, swamp));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch.params().cards()).containsExactly(plains, island, swamp);
        assertThat(firstSearch.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(firstSearch.params().reveals()).isTrue();
        assertThat(firstSearch.params().canFailToFind()).isTrue();

        pickFromLibrary(plains);

        PendingInteraction.LibrarySearch secondSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch.params().cards()).containsExactly(island, swamp);
        assertThat(secondSearch.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(secondSearch.params().reveals()).isTrue();

        pickFromLibrary(island);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, fork);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(evolvingWilds, swamp);
    }

    @Test
    @DisplayName("Finding only one basic land puts it into hand")
    void findsOnlyOneBasicLand() {
        Card plains = new Plains();
        Card evolvingWilds = new EvolvingWilds();
        castForkInTheRoad(List.of(evolvingWilds, plains));

        harness.passBothPriorities();
        pickFromLibrary(plains);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding no basic lands finishes without a library interaction")
    void findsNoBasicLands() {
        Card evolvingWilds = new EvolvingWilds();
        Card fork = castForkInTheRoad(List.of(evolvingWilds));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(evolvingWilds);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fork);
    }

    private Card castForkInTheRoad(List<Card> library) {
        Card fork = new ForkInTheRoad();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(fork));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        return fork;
    }

    private void pickFromLibrary(Card card) {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().indexOf(card);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
