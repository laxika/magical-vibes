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

    private List<Card> tenSerpents(int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new TolarianSerpent());
        }
        return library;
    }
}
