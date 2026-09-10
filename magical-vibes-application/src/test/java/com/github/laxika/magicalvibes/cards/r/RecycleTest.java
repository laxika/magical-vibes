package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Recycle.class, Forest.class, Mountain.class, LotusPetal.class})
class RecycleTest extends BaseCardTest {

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new Recycle());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UPKEEP → DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Casting a spell draws a card")
    void castingSpellDraws() {
        harness.addToBattlefield(player1, new Recycle());
        harness.setHand(player1, new ArrayList<>(List.of(new LotusPetal())));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Playing a land draws a card")
    void playingLandDraws() {
        harness.addToBattlefield(player1, new Recycle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Opponent casting a spell does not trigger the draw")
    void opponentSpellDoesNotDraw() {
        harness.addToBattlefield(player1, new Recycle());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, new ArrayList<>(List.of(new LotusPetal())));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Controller must discard down to two during cleanup")
    void controllerDiscardsDownToTwo() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Mountain(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's maximum hand size is unaffected (still seven)")
    void opponentHandSizeUnaffected() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
    }
}
