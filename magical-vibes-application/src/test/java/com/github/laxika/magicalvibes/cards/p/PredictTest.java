package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredictTest extends BaseCardTest {

    private void cast(Card topCard, List<Card> drawCards) {
        harness.setHand(player1, List.of(new Predict()));
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, drawCards);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast(createNamedCard("Top Card", "{1}{U}"), List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.NameCardMillDrawChoice.class);
    }

    @Test
    @DisplayName("Matching the chosen name draws two cards")
    void matchDrawsTwoCards() {
        Card top = createNamedCard("Named Hit", "{1}{U}");
        cast(top, List.of(createNamedCard("Draw One", "{U}"), createNamedCard("Draw Two", "{U}")));

        harness.handleListChoice(player1, "Named Hit");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A nonmatching name draws one card")
    void mismatchDrawsOneCard() {
        Card top = createNamedCard("Named Hit", "{1}{U}");
        cast(top, List.of(createNamedCard("Draw One", "{U}"), createNamedCard("Draw Two", "{U}")));

        harness.handleListChoice(player1, "Something Else");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private static Card createNamedCard(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(CardColor.BLUE);
        return card;
    }
}
