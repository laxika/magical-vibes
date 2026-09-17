package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PalantirOfOrthanc.class, GrizzlyBears.class, Shock.class, SerraAngel.class})
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

    private Permanent prepareTrigger(Card... libraryCards) {
        Permanent palantir = harness.addToBattlefieldAndReturn(player1, new PalantirOfOrthanc());
        harness.setLibrary(player1, List.of(libraryCards));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        return palantir;
    }

    private void finishScryPuttingCardsOnBottom() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
    }
}
