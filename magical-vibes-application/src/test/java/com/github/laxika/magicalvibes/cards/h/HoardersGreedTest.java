package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoardersGreed.class, Forest.class, WoodlandChangeling.class})
class HoardersGreedTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HoardersGreed()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {3}{B}
        // Opponent always reveals a mana-value-0 card, so the caster wins only when their own
        // revealed card has a strictly greater mana value.
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Losing the first clash runs the process exactly once")
    void losingFirstClashRunsOnce() {
        prepare();
        // Drawing two lands leaves a land to reveal, so neither player wins.
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castAndResolveSorcery(player1, 0, 0);
        choosePlacement(false, false);

        harness.assertLife(player1, 18); // one iteration: lost 2 life
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2); // drew two cards
    }

    @Test
    @DisplayName("Winning a clash repeats the whole process until a clash is lost")
    void winningRepeatsUntilLoss() {
        prepare();
        // The first clash reveals Woodland Changeling and wins.
        // The repeated draw consumes that creature and a land; the next clash ties.
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new WoodlandChangeling(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castAndResolveSorcery(player1, 0, 0);
        choosePlacement(false, false);

        choosePlacement(false, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Hoarder's Greed");
        harness.assertLife(player1, 16); // two iterations: lost 4 life
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4); // drew four cards total
    }

    private void choosePlacement(boolean controllerBottom, boolean opponentBottom) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                controllerBottom ? List.of() : List.of(0), controllerBottom ? List.of(0) : List.of()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(
                opponentBottom ? List.of() : List.of(0), opponentBottom ? List.of(0) : List.of()));
    }

    @Test
    @DisplayName("Bottoming a winning reveal changes the repeated draws without preventing repetition")
    void bottomingWinningRevealChangesRepeatedDraws() {
        prepare();
        WoodlandChangeling revealed = new WoodlandChangeling();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        Forest nextReveal = new Forest();
        Forest opponentReveal = new Forest();
        Forest opponentNext = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), revealed,
                firstDraw, secondDraw, nextReveal));
        harness.setLibrary(player2, List.of(opponentReveal, opponentNext));

        harness.castAndResolveSorcery(player1, 0, 0);
        choosePlacement(true, true);

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4)
                .contains(firstDraw, secondDraw).doesNotContain(revealed);
        choosePlacement(false, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextReveal, revealed);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext, opponentReveal);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Hoarder's Greed");
    }

    @Test
    @DisplayName("A higher opposing reveal stops the process after one iteration")
    void higherOpposingRevealStopsProcess() {
        prepare();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new WoodlandChangeling()));

        harness.castAndResolveSorcery(player1, 0, 0);
        choosePlacement(false, false);

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Hoarder's Greed");
    }

    @Test
    @DisplayName("Revealed cards move only after both players choose their placement")
    void revealedCardsMoveTogetherAfterBothChoices() {
        prepare();
        Forest revealed = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), revealed, next));

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        harness.assertInGraveyard(player1, "Hoarder's Greed");
    }
}
