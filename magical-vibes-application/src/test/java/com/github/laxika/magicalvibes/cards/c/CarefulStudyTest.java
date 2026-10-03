package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarefulStudy.class, Island.class})
class CarefulStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting draws two cards then discards two cards")
    void drawsTwoThenDiscardsTwo() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new CarefulStudy(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Careful Study");
    }

    @Test
    @DisplayName("With no other cards in hand, both drawn cards must be discarded")
    void discardsBothDrawnCardsWhenCastFromOtherwiseEmptyHand() {
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new CarefulStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDraw, secondDraw).hasSize(3);
        harness.assertInGraveyard(player1, "Careful Study");
        assertThat(gd.stack).isEmpty();
    }
}
