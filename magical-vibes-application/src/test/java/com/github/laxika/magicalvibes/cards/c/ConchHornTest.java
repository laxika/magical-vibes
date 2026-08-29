package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConchHorn.class})
class ConchHornTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself, draws two cards, and puts a chosen hand card on top")
    void sacrificesDrawsAndPutsCardOnTop() {
        Card firstDraw = new ConchHorn();
        Card secondDraw = new ConchHorn();
        Card handCard = new ConchHorn();
        harness.addToBattlefield(player1, new ConchHorn());
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Conch Horn");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard, firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Loses before the hand choice when a draw reaches an empty library")
    void emptyLibraryEndsGameBeforeHandChoice() {
        harness.addToBattlefield(player1, new ConchHorn());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
