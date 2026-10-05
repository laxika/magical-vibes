package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychicCorrosion.class, Island.class, Divination.class})
class PsychicCorrosionTest extends BaseCardTest {

    @Test
    @DisplayName("Your draw makes each opponent mill two cards")
    void millsOnControllerDraw() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.setLibrary(player2, List.of(new Divination(), new Island(), new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's draw does not trigger the mill")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.setLibrary(player2, List.of(new Divination(), new Island(), new Island(), new Island()));

        advanceToDraw(player2);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller never mills themselves")
    void controllerDoesNotMill() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Divination(), new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void eachCardDrawnByDivinationCreatesASeparateTrigger() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        Card first = new Island();
        Card second = new Divination();
        Card third = new Island();
        Card fourth = new Island();
        Card remaining = new Island();
        harness.setLibrary(player2, List.of(first, second, third, fourth, remaining));

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island(), new Island()));

        advanceToDraw(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void millsOnlyTheRemainingCardInAShortLibrary() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        Card lastCard = new Island();
        harness.setLibrary(player2, List.of(lastCard));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void pendingTriggerResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new PsychicCorrosion());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        advanceToDraw(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid the first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
