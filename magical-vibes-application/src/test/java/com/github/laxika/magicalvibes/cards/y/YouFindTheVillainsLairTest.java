package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouFindTheVillainsLair.class, GrizzlyBears.class, Island.class})
class YouFindTheVillainsLairTest extends BaseCardTest {

    @Test
    @DisplayName("Foil Their Scheme counters a target spell")
    void foilTheirSchemeCountersSpell() {
        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player2, List.of(new YouFindTheVillainsLair()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Learn Their Secrets draws two cards then discards two cards")
    void learnTheirSecretsDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(
                new YouFindTheVillainsLair(), new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Foil Their Scheme counters a noncreature spell without performing either draw mode")
    void foilTheirSchemeCountersInstant() {
        YouFindTheVillainsLair target = new YouFindTheVillainsLair();
        Island undrawn = new Island();
        harness.setHand(player1, List.of(target));
        harness.setLibrary(player1, List.of(undrawn, new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new YouFindTheVillainsLair()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "You Find the Villains' Lair");
        harness.assertInGraveyard(player2, "You Find the Villains' Lair");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(undrawn);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Learn Their Secrets allows discarding the newly drawn cards")
    void learnTheirSecretsCanDiscardDrawnCards() {
        Island kept = new Island();
        Island drawnFirst = new Island();
        Island drawnSecond = new Island();
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond));
        harness.setHand(player1, List.of(new YouFindTheVillainsLair(), kept));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawnFirst, drawnSecond);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnFirst, drawnSecond);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Learn Their Secrets works with no other cards initially in hand")
    void learnTheirSecretsDiscardsBothDrawsFromOtherwiseEmptyHand() {
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new YouFindTheVillainsLair()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Learn Their Secrets draws and discards for its controller on the opponent's turn")
    void learnTheirSecretsUsesControllerOnOpponentsTurn() {
        Island opponentsCard = new Island();
        harness.setHand(player1, List.of(opponentsCard));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player2, List.of(new YouFindTheVillainsLair(), new Island()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castModalInstant(player2, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
