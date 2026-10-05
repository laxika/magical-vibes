package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaddeningCacophony.class})
class MaddeningCacophonyTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, each opponent mills eight cards")
    void millsEightCardsWithoutKicker() {
        harness.setLibrary(player1, libraryOfSize(20));
        harness.setLibrary(player2, libraryOfSize(20));
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        addMana(1, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(12);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("When kicked, each opponent mills half their library rounded up")
    void millsHalfLibraryRoundedUpWhenKicked() {
        harness.setLibrary(player1, libraryOfSize(20));
        harness.setLibrary(player2, libraryOfSize(11));
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        addMana(2, 4);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 1", "2, 1", "16, 8", "20, 10"})
    @DisplayName("Kicked milling handles empty, one-card, and even-sized libraries")
    void kickedMillingHandlesLibrarySizes(int librarySize, int milledCount) {
        harness.setLibrary(player1, libraryOfSize(20));
        harness.setLibrary(player2, libraryOfSize(librarySize));
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        addMana(2, 4);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - milledCount);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(milledCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 7, 8})
    @DisplayName("Without kicker, milling stops when the opponent's library runs out")
    void millsAllAvailableCardsInShortLibrary(int librarySize) {
        harness.setLibrary(player2, libraryOfSize(librarySize));
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        addMana(1, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(librarySize);
    }

    @Test
    @DisplayName("Kicked milling uses the opponent's library size at resolution")
    void calculatesKickedAmountAtResolution() {
        harness.setLibrary(player2, libraryOfSize(20));
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        addMana(2, 4);

        harness.castKickedSorcery(player1, 0);
        harness.setLibrary(player2, libraryOfSize(9));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    private void addMana(int blue, int colorless) {
        harness.addMana(player1, ManaColor.BLUE, blue);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }

    private List<Card> libraryOfSize(int size) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            library.add(new MaddeningCacophony());
        }
        return library;
    }
}
