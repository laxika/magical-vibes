package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PryingEyes.class, Forest.class, Island.class, Mountain.class})
class PryingEyesTest extends BaseCardTest {

    @Test
    void drawsFourCardsThenPromptsForTwoDiscards() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Island()));
        harness.setHand(player1, List.of(new PryingEyes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    void completingTwoDiscardsLeavesTwoCardsInHand() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Island()));
        harness.setHand(player1, List.of(new PryingEyes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDiscardPreexistingCardsOnOpponentsTurn() {
        Forest oldForest = new Forest();
        Mountain oldMountain = new Mountain();
        Island opponentCard = new Island();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new PryingEyes(), oldForest, oldMountain));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).doesNotContain(oldForest, oldMountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldForest, oldMountain);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
