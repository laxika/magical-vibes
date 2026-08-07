package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TolarianSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Controller mills seven cards at the beginning of their upkeep")
    void upkeepMillsSeven() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, tenIslands());
        int opponentDeck = gd.playerDecks.get(player2.getId()).size();

        triggerUpkeep(player1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeck);
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, tenIslands());

        triggerUpkeep(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling a library with fewer than seven cards mills everything left")
    void millsWholeSmallLibrary() {
        harness.addToBattlefield(player1, new TolarianSerpent());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island(), new Island())));

        triggerUpkeep(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private List<Card> tenIslands() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            library.add(new Island());
        }
        return library;
    }

    private void triggerUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP queues the trigger
        harness.passBothPriorities(); // resolve the trigger
    }
}
