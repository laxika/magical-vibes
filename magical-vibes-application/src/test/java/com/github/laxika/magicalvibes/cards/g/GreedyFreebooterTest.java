package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({GreedyFreebooter.class, WrathOfGod.class, GrizzlyBears.class})
class GreedyFreebooterTest extends BaseCardTest {

    @Test
    @DisplayName("When Greedy Freebooter dies, it scries before creating a Treasure")
    void deathTriggerScriesBeforeCreatingTreasure() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new GreedyFreebooter());
        destroyFreebooter();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Greedy Freebooter can put its scried card on the bottom and still creates a Treasure")
    void deathTriggerCanPutScriedCardOnBottom() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new GreedyFreebooter());
        destroyFreebooter();

        List<Card> library = gd.playerDecks.get(player1.getId());
        Card originalTop = library.get(0);

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(library.get(library.size() - 1)).isSameAs(originalTop);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void destroyFreebooter() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
