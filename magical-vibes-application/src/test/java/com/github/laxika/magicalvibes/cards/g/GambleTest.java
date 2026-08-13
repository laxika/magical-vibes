package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GambleTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for any card, then may discard the searched card before shuffling")
    void searchesThenDiscardsBeforeShuffling() {
        Card searchedCard = new GrizzlyBears();
        Card remainingCard = new Swamp();
        harness.setHand(player1, List.of(new Gamble()));
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(List.of(searchedCard, remainingCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(searchedCard, remainingCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Gamble", "Grizzly Bears");

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        int discardLogIndex = indexOfLogContaining(logs, "discards Grizzly Bears at random");
        int shuffleLogIndex = indexOfLogContaining(logs, "shuffles their library");
        assertThat(discardLogIndex).isGreaterThanOrEqualTo(0);
        assertThat(shuffleLogIndex).isGreaterThan(discardLogIndex);
    }

    private static int indexOfLogContaining(List<String> logs, String text) {
        for (int i = 0; i < logs.size(); i++) {
            if (logs.get(i).contains(text)) {
                return i;
            }
        }
        return -1;
    }
}
