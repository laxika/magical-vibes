package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepmuckDesperado.class, Shock.class})
class DeepmuckDesperadoTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent mills three cards after the controller commits a crime")
    void millsEachOpponentAfterCrime() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        castShockAtOpponent();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(8));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Targeting yourself does not commit a crime")
    void targetingYourselfDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void castShockAtOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private List<Card> libraryWithCards(int count) {
        return IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new Shock())
                .toList();
    }
}
