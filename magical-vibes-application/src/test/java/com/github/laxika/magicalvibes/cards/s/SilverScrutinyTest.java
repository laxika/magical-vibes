package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoalitionWarbrute;
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

@CardUsed({SilverScrutiny.class, CoalitionWarbrute.class})
class SilverScrutinyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws the announced X number of cards")
    void drawsXCards() {
        List<Card> library = List.of(
                new CoalitionWarbrute(), new CoalitionWarbrute(), new CoalitionWarbrute(), new CoalitionWarbrute());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

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

    @Test
    @DisplayName("X=0 can be cast on an opponent's turn and draws no cards")
    void xZeroHasFlashAndDrawsNothing() {
        Card libraryCard = new CoalitionWarbrute();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Silver Scrutiny");
    }

    @Test
    @DisplayName("X=4 remains legal at sorcery timing and draws four cards")
    void xFourDrawsAtSorceryTiming() {
        List<Card> library = List.of(new CoalitionWarbrute(), new CoalitionWarbrute(),
                new CoalitionWarbrute(), new CoalitionWarbrute(), new CoalitionWarbrute());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, 4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
    }

    @Test
    @DisplayName("X=3 can resolve in response to another spell")
    void drawsInResponseToSpell() {
        List<Card> library = List.of(new CoalitionWarbrute(), new CoalitionWarbrute(),
                new CoalitionWarbrute(), new CoalitionWarbrute());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SilverScrutiny(), new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, 0);

        harness.castSorcery(player1, 0, 3);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
    }

    @Test
    @DisplayName("X=4 cannot be cast in response even during the caster's main phase")
    void xFourCannotRespondDuringOwnMainPhase() {
        harness.setHand(player1, List.of(new SilverScrutiny(), new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Silver Scrutiny");
    }
}
