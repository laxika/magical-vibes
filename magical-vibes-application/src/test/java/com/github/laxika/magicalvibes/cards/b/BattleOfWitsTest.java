package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattleOfWitsTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game at upkeep with exactly 200 cards in library")
    void winsWithExactlyTwoHundredCards() {
        harness.addToBattlefield(player1, new BattleOfWits());
        setLibrarySize(player1, 200);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Battle of Wits");

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("wins the game"));
    }

    @Test
    @DisplayName("Wins the game at upkeep with more than 200 cards in library")
    void winsWithMoreThanTwoHundredCards() {
        harness.addToBattlefield(player1, new BattleOfWits());
        setLibrarySize(player1, 250);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with fewer than 200 cards in library")
    void doesNotTriggerWithOneHundredNinetyNineCards() {
        harness.addToBattlefield(player1, new BattleOfWits());
        setLibrarySize(player1, 199);

        advanceToUpkeep(player1);

        // Intervening-if fails: no trigger on the stack
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BattleOfWits());
        setLibrarySize(player1, 200);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Condition re-checked on resolution — no win if library drops below 200")
    void interveningIfCheckedOnResolution() {
        harness.addToBattlefield(player1, new BattleOfWits());
        setLibrarySize(player1, 200);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        // Drop below 200 before the trigger resolves
        setLibrarySize(player1, 199);

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Helpers =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private void setLibrarySize(Player player, int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new Card());
        }
        harness.setLibrary(player, library);
    }
}
