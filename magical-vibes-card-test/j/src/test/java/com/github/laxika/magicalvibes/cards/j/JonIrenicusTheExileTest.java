package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JonIrenicusTheExile.class, GrizzlyBears.class})
class JonIrenicusTheExileTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when your library is larger than the target opponent's")
    void drawsWhenOwnLibraryIsLarger() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new JonIrenicusTheExile());

        resolveTriggerAgainst(player2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Otherwise each opponent mills five cards")
    void otherwiseEachOpponentMillsFive() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, cards(6));
        harness.addToBattlefield(player1, new JonIrenicusTheExile());

        resolveTriggerAgainst(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The triggered ability can target only an opponent")
    void targetsOnlyOpponent() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new JonIrenicusTheExile());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
    }

    @Test
    void equalLibrariesMillOnlyTheOpponentAndStopAtTheEndOfTheirLibrary() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards(3));
        List<Card> opponentLibrary = cards(3);
        harness.setLibrary(player2, opponentLibrary);
        harness.addToBattlefield(player1, new JonIrenicusTheExile());

        resolveTriggerAgainst(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }

    @Test
    void emptyLibrariesDoNotCauseADraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new JonIrenicusTheExile());

        resolveTriggerAgainst(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void comparesLibrariesAtResolutionRatherThanWhenTheAbilityTriggers() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, new GrizzlyBears()));
        harness.setLibrary(player2, cards(6));
        harness.addToBattlefield(player1, new JonIrenicusTheExile());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            harness.handlePermanentChosen(player1, player2.getId());
            harness.setLibrary(player2, cards(1));
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringTheOpponentsEndStep() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards(2));
        harness.setLibrary(player2, cards(1));
        harness.addToBattlefield(player1, new JonIrenicusTheExile());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void resolveTriggerAgainst(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });
    }

    private List<Card> cards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }
}
