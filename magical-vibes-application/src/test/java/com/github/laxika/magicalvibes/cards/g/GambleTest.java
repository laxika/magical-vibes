package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Gamble.class)
class GambleTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for any card, then discards a card at random before shuffling")
    void searchesThenDiscardsBeforeShuffling() {
        Card gamble = new Gamble();
        Card searchedCard = new Gamble();
        Card remainingCard = new Gamble();
        harness.setLibrary(player1, List.of(searchedCard, remainingCard));
        harness.castFromHand(player1, gamble, "{R}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(searchedCard, remainingCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(gamble, searchedCard);

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        int discardLogIndex = indexOfLogContaining(logs, "discards Gamble at random");
        int shuffleLogIndex = indexOfLogContaining(logs, "shuffles their library");
        assertThat(discardLogIndex).isGreaterThanOrEqualTo(0);
        assertThat(shuffleLogIndex).isGreaterThan(discardLogIndex);
    }

    @Test
    @DisplayName("Discards exactly one card from the post-search hand")
    void discardsOneCardFromPostSearchHand() {
        Card gamble = new Gamble();
        Card existingHandCard = new Gamble();
        Card searchedCard = new Gamble();
        harness.setHand(player1, List.of(gamble, existingHandCard));
        harness.setLibrary(player1, List.of(searchedCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .containsAnyOf(existingHandCard, searchedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .contains(gamble)
                .containsAnyOf(existingHandCard, searchedCard);
    }

    @Test
    @DisplayName("Still discards and shuffles when the library is empty")
    void discardsAndShufflesWithEmptyLibrary() {
        Card gamble = new Gamble();
        Card handCard = new Gamble();
        harness.setHand(player1, List.of(gamble, handCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(gamble, handCard);

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        int discardLogIndex = indexOfLogContaining(logs, "discards Gamble at random");
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
