package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TolarianSerpent.class)
class TolarianSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Controller mills seven cards at the beginning of their upkeep")
    void upkeepMillsSeven() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, tenSerpents(10));
        int opponentDeck = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeck);
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, tenSerpents(10));

        advanceToUpkeep(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling a library with fewer than seven cards mills everything left")
    void millsWholeSmallLibrary() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, tenSerpents(3));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The upkeep ability uses the stack and mills the top seven cards")
    void millsTopCardsOnlyAfterResolution() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        List<Card> library = tenSerpents(10);
        harness.setLibrary(player1, library);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 7));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(library.subList(7, 10));
    }

    @Test
    @DisplayName("Each Serpent independently mills seven cards")
    void multipleSerpentsTriggerIndependently() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, tenSerpents(20));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(13);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(14);
    }

    @Test
    @DisplayName("An empty library does not prevent the upkeep trigger from resolving")
    void emptyLibraryStillResolves() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private List<Card> tenSerpents(int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new TolarianSerpent());
        }
        return library;
    }
}
