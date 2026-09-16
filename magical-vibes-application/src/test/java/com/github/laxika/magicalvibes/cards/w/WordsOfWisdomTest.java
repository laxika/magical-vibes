package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WordsOfWisdom.class)
class WordsOfWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws two cards and each other player draws one")
    void controllerDrawsTwoAndOtherPlayersDrawOne() {
        harness.setLibrary(player1, List.of(new WordsOfWisdom(), new WordsOfWisdom()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new WordsOfWisdom()));

        harness.castFromHand(player1, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Words of Wisdom");
    }
}
