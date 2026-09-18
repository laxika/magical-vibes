package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncestorsChosen;
import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoralityShift.class, AncestorsChosen.class, BenevolentBodyguard.class})
class MoralityShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges the controller's graveyard and library, then shuffles the new library")
    void exchangesGraveyardAndLibrary() {
        Card libraryCard = new BenevolentBodyguard();
        Card graveyardCard = new AncestorsChosen();
        Card moralityShift = new MoralityShift();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.castFromHand(player1, moralityShift, "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, moralityShift);
    }

    @Test
    @DisplayName("Only exchanges the controller's zones")
    void opponentZonesAreUntouched() {
        Card opponentLibraryCard = new BenevolentBodyguard();
        Card opponentGraveyardCard = new AncestorsChosen();
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player1, List.of(new BenevolentBodyguard()));
        harness.setGraveyard(player1, List.of(new AncestorsChosen()));
        harness.castFromHand(player1, new MoralityShift(), "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }

    @Test
    @DisplayName("Handles an empty graveyard")
    void handlesEmptyGraveyard() {
        Card libraryCard = new BenevolentBodyguard();
        Card moralityShift = new MoralityShift();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, moralityShift, "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, moralityShift);
    }
}
