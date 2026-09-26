package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevivingDose.class, GrizzlyBears.class})
class RevivingDoseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reviving Dose gains 3 life and draws a card")
    void resolvingGainsLifeAndDraws() {
        harness.setLife(player1, 17);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castFromHand(player1, new RevivingDose(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Reviving Dose");
    }

    @Test
    @DisplayName("Reviving Dose affects only its controller")
    void affectsOnlyItsController() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 17);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castFromHand(player1, new RevivingDose(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }
}
