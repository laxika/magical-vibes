package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalantirOfOrthanc.class, GrizzlyBears.class, Shock.class, SerraAngel.class,
        RestInPeace.class, NarsetParterOfVeils.class})
class PalantirOfOrthancTest extends BaseCardTest {

    @Test
    void endStepAddsInfluenceCounterAndScriesTwo() {
        Permanent palantir = prepareTrigger(new GrizzlyBears(), new Shock(), new SerraAngel());

        assertThat(palantir.getCounterCount(CounterType.INFLUENCE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void targetOpponentAcceptsAndControllerDraws() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card drawn = new SerraAngel();
        prepareTrigger(first, second, drawn);
        finishScryPuttingCardsOnBottom();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void decliningMillsInfluenceCountAndLosesMilledManaValue() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card milled = new SerraAngel();
        Permanent palantir = prepareTrigger(first, second, milled);
        finishScryPuttingCardsOnBottom();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(palantir.getCounterCount(CounterType.INFLUENCE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(milled);
    }

    @Test
    void decliningMillsAllInfluenceCountersAndSumsManaValues() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new SerraAngel();
        Permanent palantir = prepareTrigger(first, second, third);
        palantir.setCounterCount(CounterType.INFLUENCE, 3);
        finishScryPuttingCardsOnBottom();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningWithMoreCountersThanCardsMillsOnlyAvailableCards() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Permanent palantir = prepareTrigger(first, second);
        palantir.setCounterCount(CounterType.INFLUENCE, 5);
        finishScryPuttingCardsOnBottom();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningStillCountsManaValueWhenMilledCardIsExiledInstead() {
        harness.addToBattlefield(player1, new RestInPeace());
        Card milled = new SerraAngel();
        prepareTrigger(new GrizzlyBears(), new Shock(), milled);
        finishScryPuttingCardsOnBottom();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(milled);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void opponentUnableToDrawCanStillChooseForControllerToDraw() {
        harness.addToBattlefield(player1, new NarsetParterOfVeils());
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);
        Card drawn = new SerraAngel();
        prepareTrigger(new GrizzlyBears(), new Shock(), drawn);
        finishScryPuttingCardsOnBottom();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void controllerUnableToDrawMustMillAndOpponentLosesLife() {
        harness.addToBattlefield(player2, new NarsetParterOfVeils());
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        Card milled = new SerraAngel();
        prepareTrigger(new GrizzlyBears(), new Shock(), milled);
        finishScryPuttingCardsOnBottom();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void opponentsEndStepDoesNotTrigger() {
        Permanent palantir = harness.addToBattlefieldAndReturn(player1, new PalantirOfOrthanc());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(palantir.getCounterCount(CounterType.INFLUENCE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent prepareTrigger(Card... libraryCards) {
        Permanent palantir = harness.addToBattlefieldAndReturn(player1, new PalantirOfOrthanc());
        harness.setLibrary(player1, List.of(libraryCards));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        return palantir;
    }

    private void finishScryPuttingCardsOnBottom() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
    }
}
