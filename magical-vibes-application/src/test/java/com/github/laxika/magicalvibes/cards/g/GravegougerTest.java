package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gravegouger.class, GrizzlyBears.class})
class GravegougerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to two cards from one graveyard and leaves-the-battlefield returns them")
    void exiledCardsReturnToTheirOwnersGraveyardsWhenGravegougerLeaves() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        castGravegouger();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);

        Permanent gravegouger = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gravegouger));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Gravegouger");
    }

    @Test
    @DisplayName("The ETB cards must be chosen from a single graveyard")
    void etbCardsMustShareGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castGravegouger();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownCard.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castGravegouger() {
        harness.setHand(player1, List.of(new Gravegouger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }
}
