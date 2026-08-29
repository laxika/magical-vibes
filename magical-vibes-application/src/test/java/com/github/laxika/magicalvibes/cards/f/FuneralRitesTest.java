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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstMilled, secondMilled);
        harness.assertInGraveyard(player1, "Funeral Rites");
    }
}
