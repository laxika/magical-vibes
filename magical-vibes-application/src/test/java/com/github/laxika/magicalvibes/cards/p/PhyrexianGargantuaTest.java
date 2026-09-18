package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
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
        harness.passBothPriorities(); // resolve creature spell and queue the ETB ability
        harness.passBothPriorities(); // resolve the ETB ability

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
        harness.passBothPriorities(); // resolve creature spell and queue the ETB ability
        harness.passBothPriorities(); // resolve the ETB ability

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent is unaffected by the ETB trigger")
    void opponentUnaffected() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castGargantua();
        harness.passBothPriorities(); // resolve creature spell and queue the ETB ability
        harness.passBothPriorities(); // resolve the ETB ability

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(opponentHandBefore);
    }

    private void castGargantua() {
        harness.castFromHand(player1, new PhyrexianGargantua(), "{4}{B}{B}");
    }
}
