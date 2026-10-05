package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianGargantua.class, Forest.class})
class PhyrexianGargantuaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the controller draw two cards and lose 2 life")
    void etbDrawsTwoAndLosesTwoLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castGargantua(); // setHand leaves the hand empty after this spell is cast
        int handBefore = gd.playerHands.get(player1.getId()).size();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB draws the available card and still causes 2 life loss from a one-card library")
    void etbDrawsAvailableCardAndLosesLifeWithOneCardLibrary() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castGargantua();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the trigger's life loss before the controller loses")
    void emptyLibraryStillLosesLife() {
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castGargantua();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The controller draws both cards before losing the game to the trigger's life loss")
    void lethalLifeLossStillDrawsBothCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLife(player1, 2);

        castGargantua();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent is unaffected by the ETB trigger")
    void opponentUnaffected() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castGargantua();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(opponentHandBefore);
    }

    private void castGargantua() {
        harness.castFromHand(player1, new PhyrexianGargantua(), "{4}{B}{B}");
    }
}
