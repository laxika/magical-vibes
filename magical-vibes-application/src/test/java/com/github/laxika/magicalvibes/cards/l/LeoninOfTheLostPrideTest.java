package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninOfTheLostPride.class, GrizzlyBears.class, WrathOfGod.class})
class LeoninOfTheLostPrideTest extends BaseCardTest {

    @Test
    @DisplayName("When Leonin of the Lost Pride dies, it exiles a targeted card from an opponent's graveyard")
    void deathExilesOpponentGraveyardCard() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        destroyLeonin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Only opponent graveyard cards are legal targets")
    void ownGraveyardCardIsNotTargetable() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCard)));

        destroyLeonin();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(opponentCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(ownCard.getId());
    }

    @Test
    @DisplayName("No opponent graveyard card means the death trigger is not put on the stack")
    void noOpponentGraveyardCardSkipsTrigger() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        destroyLeonin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void destroyLeonin() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
