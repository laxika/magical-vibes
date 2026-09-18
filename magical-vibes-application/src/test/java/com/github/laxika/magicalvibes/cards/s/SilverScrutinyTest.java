package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverScrutiny.class, GrizzlyBears.class})
class SilverScrutinyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws the announced X number of cards")
    void drawsXCards() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorceryForX(player1, 0, 3, Map.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
    }

    @Test
    @DisplayName("X=3 allows casting during an opponent's turn")
    void xThreeHasFlashTiming() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.passPriority(player2);
        harness.castSorceryForX(player1, 0, 3, Map.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("X=4 does not allow instant-speed casting")
    void xFourKeepsSorceryTiming() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castSorceryForX(player1, 0, 4, Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
