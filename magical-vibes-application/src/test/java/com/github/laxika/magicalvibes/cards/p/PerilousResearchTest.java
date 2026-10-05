package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerilousResearch.class, SnowCoveredForest.class, SnowCoveredIsland.class})
class PerilousResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards before the controller chooses a permanent to sacrifice")
    void drawsThenSacrificesPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredForest()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        PerilousResearch spell = new PerilousResearch();

        harness.castFromHand(player1, spell, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard(), spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices the controller's only permanent and leaves the opponent's permanent alone")
    void sacrificesOnlyControllersPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        Permanent opposingIsland = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        SnowCoveredIsland drawnIsland = new SnowCoveredIsland();
        SnowCoveredForest drawnForest = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(drawnIsland, drawnForest));
        PerilousResearch spell = new PerilousResearch();

        harness.castFromHand(player1, spell, "{1}{U}");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(drawnIsland, drawnForest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingIsland);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard(), spell);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Draws two cards without prompting when the controller has no permanent")
    void drawsTwoCardsWithoutPermanentToSacrifice() {
        SnowCoveredIsland drawnIsland = new SnowCoveredIsland();
        SnowCoveredForest drawnForest = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(drawnIsland, drawnForest));
        PerilousResearch spell = new PerilousResearch();

        harness.castFromHand(player1, spell, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(drawnIsland, drawnForest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }
}
