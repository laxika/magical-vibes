package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FuneralRites.class)
class FuneralRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, loses 2 life, then mills two cards")
    void drawsLosesLifeThenMills() {
        Card firstDrawn = new FuneralRites();
        Card secondDrawn = new FuneralRites();
        Card firstMilled = new FuneralRites();
        Card secondMilled = new FuneralRites();
        harness.setHand(player1, List.of(new FuneralRites()));
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn, firstMilled, secondMilled));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstMilled, secondMilled);
        harness.assertInGraveyard(player1, "Funeral Rites");
    }

    @Test
    @DisplayName("Mills only the remaining card when drawing leaves one card in the library")
    void millsOnlyRemainingCard() {
        Card spell = new FuneralRites();
        Card firstDrawn = new FuneralRites();
        Card secondDrawn = new FuneralRites();
        Card remaining = new FuneralRites();
        Card opponentLibraryCard = new FuneralRites();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn, remaining));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(remaining, spell);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
    }

    @Test
    @DisplayName("Still loses life when drawing empties the library before milling")
    void resolvesWithNothingLeftToMill() {
        Card spell = new FuneralRites();
        Card firstDrawn = new FuneralRites();
        Card secondDrawn = new FuneralRites();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        harness.assertLife(player1, 18);
    }
}
