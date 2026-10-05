package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulseOfTheGrid.class, DarksteelCitadel.class})
class PulseOfTheGridTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, discards one, and returns to hand when an opponent has more cards")
    void returnsToHandWhenOpponentHasMoreCards() {
        PulseOfTheGrid pulse = new PulseOfTheGrid();
        harness.setHand(player1, List.of(pulse, new DarksteelCitadel(), new DarksteelCitadel()));
        harness.setHand(player2, List.of(new DarksteelCitadel(), new DarksteelCitadel(),
                new DarksteelCitadel(), new DarksteelCitadel()));
        harness.setLibrary(player1, List.of(new DarksteelCitadel(), new DarksteelCitadel()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    @Test
    @DisplayName("Goes to the graveyard when no opponent has more cards after the discard")
    void goesToGraveyardWhenOpponentDoesNotHaveMoreCards() {
        PulseOfTheGrid pulse = new PulseOfTheGrid();
        harness.setHand(player1, List.of(pulse, new DarksteelCitadel(), new DarksteelCitadel()));
        harness.setHand(player2, List.of(new DarksteelCitadel(), new DarksteelCitadel(),
                new DarksteelCitadel()));
        harness.setLibrary(player1, List.of(new DarksteelCitadel(), new DarksteelCitadel()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse);
    }

    @Test
    @DisplayName("Can discard a newly drawn card before comparing hand sizes")
    void canDiscardNewlyDrawnCard() {
        PulseOfTheGrid pulse = new PulseOfTheGrid();
        DarksteelCitadel firstDraw = new DarksteelCitadel();
        DarksteelCitadel secondDraw = new DarksteelCitadel();
        harness.setHand(player1, List.of(pulse));
        harness.setHand(player2, List.of(new DarksteelCitadel(), new DarksteelCitadel()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not return when drawing and discarding leaves a larger hand than the opponent")
    void goesToGraveyardWhenControllerHasMoreCards() {
        PulseOfTheGrid pulse = new PulseOfTheGrid();
        DarksteelCitadel keptCard = new DarksteelCitadel();
        harness.setHand(player1, List.of(pulse, keptCard));
        harness.setHand(player2, List.of(new DarksteelCitadel()));
        harness.setLibrary(player1, List.of(new DarksteelCitadel(), new DarksteelCitadel()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).doesNotContain(pulse, keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(keptCard, pulse);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
