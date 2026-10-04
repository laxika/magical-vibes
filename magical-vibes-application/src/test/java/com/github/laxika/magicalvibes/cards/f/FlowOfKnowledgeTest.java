package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowOfKnowledge.class, Island.class, Swamp.class})
class FlowOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws per controlled Island, then discards two cards")
    void drawsPerIslandThenDiscardsTwo() {
        harness.setLibrary(player1, List.of(new Island(), new Swamp(), new Island()));
        harness.setHand(player1, List.of(new FlowOfKnowledge(), new Swamp(), new Swamp()));
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void discardsTwoEvenWithoutControlledIslands() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new FlowOfKnowledge(), new Swamp(), new Swamp(), new Island()));
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardsOnlyAvailableCardAfterDrawingOne() {
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn, new Swamp()));
        harness.setHand(player1, List.of(new FlowOfKnowledge()));
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithNoIslandsAndNoCardsToDiscard() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new FlowOfKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canChooseNewlyDrawnCardsToDiscard() {
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        Swamp kept = new Swamp();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Swamp()));
        harness.setHand(player1, List.of(new FlowOfKnowledge(), kept));
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDraw, secondDraw).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
