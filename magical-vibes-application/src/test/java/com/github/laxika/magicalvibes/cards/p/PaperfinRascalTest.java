package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaperfinRascal.class, Forest.class})
class PaperfinRascalTest extends BaseCardTest {

    private Permanent castPaperfinRascal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PaperfinRascal(), "{2}{U}");
        resolveAllTriggers();
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry choice) {
            gs.handleInteractionAnswer(gd, choice.playerId().equals(player1.getId()) ? player1 : player2,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }

        return findPermanent(player1, "Paperfin Rascal");
    }

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Paperfin Rascal")
    void wonClashAddsCounter() {
        // Higher mana value on top for player1 (Paperfin Rascal MV 3 > Forest MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new PaperfinRascal()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rascal.getEffectivePower()).isEqualTo(3);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing the clash leaves Paperfin Rascal without a counter")
    void lostClashAddsNoCounter() {
        // Lower mana value on top for player1 (Forest MV 0 < Paperfin Rascal MV 3) → player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(rascal.getEffectivePower()).isEqualTo(2);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An equal mana value tie is not a win, so no counter is added")
    void tiedClashAddsNoCounter() {
        // Equal mana values (both Paperfin Rascals MV 3) → no one wins the clash.
        harness.setLibrary(player1, List.of(new PaperfinRascal()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("An empty library cannot win the clash, so no counter is added")
    void emptyLibraryAddsNoCounter() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(rascal.getEffectivePower()).isEqualTo(2);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Revealing a card wins against an empty opponent library")
    void opponentEmptyLibraryAllowsWin() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bottoming revealed cards does not change the clash winner")
    void bottomingCardsPreservesWin() {
        PaperfinRascal revealed = new PaperfinRascal();
        Forest next = new Forest();
        Forest opponentRevealed = new Forest();
        PaperfinRascal opponentNext = new PaperfinRascal();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(opponentRevealed, opponentNext));
        harness.castFromHand(player1, new PaperfinRascal(), "{2}{U}");
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext, opponentRevealed);
        assertThat(findPermanent(player1, "Paperfin Rascal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The active player chooses placement first even when the opponent controls the trigger")
    void activePlayerChoosesFirstForOpponentsTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));
        harness.enterBattlefieldAndReturn(player2, new PaperfinRascal());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(findPermanent(player2, "Paperfin Rascal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Neither revealed card moves before both players choose placement")
    void placementWaitsForBothChoices() {
        PaperfinRascal revealed = new PaperfinRascal();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest(), new PaperfinRascal()));
        harness.castFromHand(player1, new PaperfinRascal(), "{2}{U}");
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
    }
}
