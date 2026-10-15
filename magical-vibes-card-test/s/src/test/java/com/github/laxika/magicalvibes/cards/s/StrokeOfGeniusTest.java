package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrokeOfGenius.class, CoralMerfolk.class})
class StrokeOfGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws X cards")
    void targetPlayerDrawsXCards() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 6); // X=3: {3}{2}{U} = 6
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("With X=0, target player draws no cards")
    void xZeroDrawsNoCards() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castInstant(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent merfolk = addCreatureReady(player2, new CoralMerfolk());

        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 does not attempt to draw from an empty library")
    void zeroWithEmptyLibraryDoesNotLose() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
        harness.assertInGraveyard(player1, "Stroke of Genius");
    }

    @Test
    @DisplayName("Drawing exactly the remaining library does not cause a loss")
    void drawingExactlyRemainingLibraryDoesNotLose() {
        CoralMerfolk first = new CoralMerfolk();
        CoralMerfolk second = new CoralMerfolk();
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Attempting to draw more than the remaining library makes the target lose")
    void drawingBeyondRemainingLibraryMakesTargetLose() {
        CoralMerfolk remaining = new CoralMerfolk();
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(remaining));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
