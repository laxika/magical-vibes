package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CarefulStudy;
import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Predict.class, Peek.class, CarefulStudy.class, Concentrate.class})
class PredictTest extends BaseCardTest {

    private void cast(Card topCard, List<Card> drawCards) {
        cast(List.of(topCard), drawCards);
    }

    private void cast(List<Card> targetLibrary, List<Card> drawCards) {
        harness.setHand(player1, List.of(new Predict()));
        harness.setLibrary(player2, targetLibrary);
        harness.setLibrary(player1, drawCards);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast(new Peek(), List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.NameCardMillDrawChoice.class);
    }

    @Test
    @DisplayName("Matching the chosen name draws two cards")
    void matchDrawsTwoCards() {
        Card top = new Peek();
        cast(top, List.of(new CarefulStudy(), new Concentrate()));

        harness.handleListChoice(player1, "Peek");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A nonmatching name draws one card")
    void mismatchDrawsOneCard() {
        Card top = new Concentrate();
        cast(top, List.of(new Peek(), new CarefulStudy()));

        harness.handleListChoice(player1, "Peek");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Targeting yourself is allowed because the spell targets a player")
    void canTargetYourself() {
        Card top = new Peek();
        Card drawOne = new CarefulStudy();
        Card drawTwo = new Concentrate();

        harness.setHand(player1, List.of(new Predict()));
        harness.setLibrary(player1, List.of(top, drawOne, drawTwo));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Peek");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawOne, drawTwo);
    }

    @Test
    @DisplayName("An empty target library still draws one card")
    void emptyTargetLibraryDrawsOneCard() {
        cast(List.of(), List.of(new Peek()));

        harness.handleListChoice(player1, "Peek");

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
