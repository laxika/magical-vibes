package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarvenPatrol;
import com.github.laxika.magicalvibes.cards.e.EvasiveAction;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinRingleader.class, GoblinLegionnaire.class, DwarvenPatrol.class, EvasiveAction.class,
        WoodlandChangeling.class, Tarfire.class})
class GoblinRingleaderTest extends BaseCardTest {

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castRingleader() {
        harness.castFromHand(player1, new GoblinRingleader(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Goblin cards among the top four go to hand, the rest go to the bottom")
    void goblinsGoToHand() {
        GoblinLegionnaire goblin1 = new GoblinLegionnaire();
        GoblinLegionnaire goblin2 = new GoblinLegionnaire();
        DwarvenPatrol nonGoblinCreature = new DwarvenPatrol();
        EvasiveAction nonGoblinInstant = new EvasiveAction();
        DwarvenPatrol unrevealedCard = new DwarvenPatrol();
        harness.setLibrary(player1, List.of(
                goblin1, goblin2, nonGoblinCreature, nonGoblinInstant, unrevealedCard));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(goblin1, goblin2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unrevealedCard, nonGoblinCreature, nonGoblinInstant);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        DwarvenPatrol nonGoblin1 = new DwarvenPatrol();
        DwarvenPatrol nonGoblin2 = new DwarvenPatrol();
        DwarvenPatrol nonGoblin3 = new DwarvenPatrol();
        DwarvenPatrol nonGoblin4 = new DwarvenPatrol();
        GoblinLegionnaire deepGoblin = new GoblinLegionnaire();
        harness.setLibrary(player1, List.of(
                nonGoblin1, nonGoblin2, nonGoblin3, nonGoblin4, deepGoblin));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(deepGoblin, nonGoblin1, nonGoblin2, nonGoblin3, nonGoblin4);
    }

    @Test
    @DisplayName("A changeling card counts as a Goblin card")
    void changelingCountsAsGoblin() {
        WoodlandChangeling changeling = new WoodlandChangeling();
        EvasiveAction nonGoblinCard = new EvasiveAction();
        harness.setLibrary(player1, List.of(changeling, nonGoblinCard));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeling);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonGoblinCard);
    }

    @Test
    @DisplayName("A noncreature Goblin card is still put into hand")
    void nonCreatureGoblinCardGoesToHand() {
        Tarfire goblinTribal = new Tarfire();
        harness.setLibrary(player1, List.of(goblinTribal));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(goblinTribal);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not create a reorder interaction")
    void emptyLibraryDoesNotCreateReorderInteraction() {
        harness.setLibrary(player1, List.of());

        castRingleader();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
