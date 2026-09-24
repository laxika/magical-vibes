package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TinybonesTrinketThief.class, Distress.class, GrizzlyBears.class})
class TinybonesTrinketThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life at each end step after an opponent discarded")
    void drawsAndLosesLifeAfterOpponentDiscarded() {
        harness.addToBattlefield(player1, new TinybonesTrinketThief());
        harness.setHand(player1, List.of(new Distress()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when no opponent discarded this turn")
    void doesNotTriggerWithoutOpponentDiscard() {
        harness.addToBattlefield(player1, new TinybonesTrinketThief());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Makes only opponents with empty hands lose 10 life")
    void emptiesHandOpponentsLoseLife() {
        harness.addToBattlefield(player1, new TinybonesTrinketThief());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Does not affect opponents who have cards in hand")
    void opponentsWithCardsInHandDoNotLoseLife() {
        harness.addToBattlefield(player1, new TinybonesTrinketThief());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
