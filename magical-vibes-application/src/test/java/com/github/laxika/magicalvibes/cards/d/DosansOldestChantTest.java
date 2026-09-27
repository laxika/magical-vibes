package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DosansOldestChant.class, ArabaMothrider.class})
class DosansOldestChantTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life and draws a card")
    void gainsLifeAndDrawsCard() {
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new DosansOldestChant(), "{4}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Drawing from an empty library causes the controller to lose")
    void drawingFromEmptyLibraryCausesLoss() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new DosansOldestChant(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
