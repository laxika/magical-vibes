package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StockmanMadFlyEntist.class, GrizzlyBears.class, Island.class})
class StockmanMadFlyEntistTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersDrawsThenDiscards() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Island();
        harness.setHand(player1, List.of(new StockmanMadFlyEntist(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, drawn);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Islandcycling searches for an Island and discards Stockman")
    void islandcyclingSearchesForIsland() {
        Card stockman = new StockmanMadFlyEntist();
        Island island = new Island();
        harness.setHand(player1, List.of(stockman));
        harness.setLibrary(player1, List.of(island, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(stockman);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(card -> card instanceof Island);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(island);
    }
}
