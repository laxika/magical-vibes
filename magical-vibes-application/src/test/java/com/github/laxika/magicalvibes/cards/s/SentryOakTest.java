package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudgoatRanger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentryOak.class, CloudgoatRanger.class, Forest.class})
class SentryOakTest extends BaseCardTest {

    private Permanent sentryOak() {
        return findPermanent(player1, "Sentry Oak");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void keepBothRevealedCardsOnTop() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Bottoming both revealed cards does not change the clash winner")
    void bottomingRevealedCardsPreservesWin() {
        harness.addToBattlefield(player1, new SentryOak());
        CloudgoatRanger winningCard = new CloudgoatRanger();
        Forest ownNextCard = new Forest();
        Forest losingCard = new Forest();
        CloudgoatRanger opposingNextCard = new CloudgoatRanger();
        harness.setLibrary(player1, List.of(winningCard, ownNextCard));
        harness.setLibrary(player2, List.of(losingCard, opposingNextCard));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownNextCard, winningCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingNextCard, losingCard);
        assertThat(sentryOak().getPowerModifier()).isEqualTo(2);
        assertThat(sentryOak().hasKeyword(Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Revealing a land wins against an opponent with an empty library")
    void landWinsAgainstEmptyLibrary() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(sentryOak().getPowerModifier()).isEqualTo(2);
        assertThat(sentryOak().hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library cannot win a clash")
    void emptyLibraryDoesNotWin() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(sentryOak().getPowerModifier()).isEqualTo(0);
        assertThat(sentryOak().hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    @Test
    @DisplayName("Winning the clash gives +2/+0 and removes defender until end of turn")
    void wonClashBoostsAndRemovesDefender() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of(new CloudgoatRanger()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        keepBothRevealedCardsOnTop();

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(2);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Losing the clash leaves Sentry Oak unboosted and still with defender")
    void lostClashNoChange() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new CloudgoatRanger()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        keepBothRevealedCardsOnTop();

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("A tied clash leaves Sentry Oak unchanged")
    void tiedClashNoChange() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of(new CloudgoatRanger()));
        harness.setLibrary(player2, List.of(new CloudgoatRanger()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        keepBothRevealedCardsOnTop();

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Declining the clash leaves Sentry Oak unchanged")
    void declineClashNoChange() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of(new CloudgoatRanger()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new SentryOak());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The boost and defender loss wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SentryOak());
        harness.setLibrary(player1, List.of(new CloudgoatRanger()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        keepBothRevealedCardsOnTop();

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(2);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }
}
