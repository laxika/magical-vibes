package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FateUnraveler.class, Divination.class})
class FateUnravelerTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Opponent draw step draw causes 1 damage")
    void triggersOnOpponentDrawStepDraw() {
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Controller draw step draw does not trigger Fate Unraveler")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.setLife(player1, 20);

        advanceToDraw(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent drawing two cards from a spell takes 2 damage")
    void triggersPerCardDrawnFromSpell() {
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Draw triggers deal damage separately after the draw spell resolves")
    void damageWaitsForEachTriggerToResolve() {
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Fate Unraveler triggers independently for an opponent's draw")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Controller drawing two cards from a spell does not trigger")
    void controllerDrawSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FateUnraveler());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
