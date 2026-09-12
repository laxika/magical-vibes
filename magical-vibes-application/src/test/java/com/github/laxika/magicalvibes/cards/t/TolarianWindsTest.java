package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolarianWinds.class, CoralMerfolk.class, Island.class})
class TolarianWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting discards remaining hand then draws that many cards")
    void discardsHandThenDrawsThatMany() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(
                new TolarianWinds(),
                new CoralMerfolk(),
                new CoralMerfolk(),
                new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        // After casting, hand had 3 cards (spell left hand). Discard 3, draw 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Tolarian Winds");
        harness.assertInGraveyard(player1, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("With empty hand after casting, discards nothing and draws nothing")
    void emptyHandDoesNothing() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TolarianWinds()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Tolarian Winds");
    }

    @Test
    @DisplayName("Discards only the controller's hand")
    void onlyControllerHandChanges() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TolarianWinds(), new CoralMerfolk()));
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Coral Merfolk");
    }
}
