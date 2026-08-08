package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicCorrosionTest extends BaseCardTest {

    @Test
    @DisplayName("Your draw makes each opponent mill two cards")
    void millsOnControllerDraw() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        setDeck(player2, List.<Card>of(new GrizzlyBears(), new Island(), new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's draw does not trigger the mill")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        setDeck(player2, List.<Card>of(new GrizzlyBears(), new Island(), new Island(), new Island()));

        advanceToDraw(player2);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller never mills themselves")
    void controllerDoesNotMill() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        setDeck(player1, List.<Card>of(new Island(), new Island(), new Island(), new Island()));
        setDeck(player2, List.<Card>of(new GrizzlyBears(), new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void setDeck(Player player, List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid the first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }
}
