package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwinbladeAssassinsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at its controller's end step when a creature died this turn")
    void drawsAtControllerEndStepWhenCreatureDied() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        setDeck(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutCreatureDeath() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        setDeck(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        setDeck(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
