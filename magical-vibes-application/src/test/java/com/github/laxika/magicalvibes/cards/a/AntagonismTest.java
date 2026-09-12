package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.ShivanGorge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Antagonism.class, ShivanGorge.class})
class AntagonismTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the player whose end step it is when no opponent was damaged")
    void damagesEndStepPlayerWhenNoOpponentWasDamaged() {
        harness.addToBattlefield(player1, new Antagonism());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to its controller on their end step when no opponent was damaged")
    void damagesControllerOnOwnEndStepWhenNoOpponentWasDamaged() {
        harness.addToBattlefield(player1, new Antagonism());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Checks the end-step player's opponents rather than the enchantment controller's opponents")
    void checksEndStepPlayersOpponents() {
        harness.addToBattlefield(player1, new Antagonism());
        harness.addToBattlefield(player2, new ShivanGorge());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Checks whether an opponent was damaged when the trigger resolves")
    void damageAfterTriggerIsPutOnStackPreventsDamage() {
        harness.addToBattlefield(player1, new Antagonism());
        harness.addToBattlefield(player2, new ShivanGorge());

        advanceToEndStep(player2);

        harness.addMana(player2, ManaColor.RED, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts damage dealt earlier in the turn before Antagonism entered the battlefield")
    void damageBeforeAntagonismEnteredBattlefieldPreventsDamage() {
        harness.addToBattlefield(player2, new ShivanGorge());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Antagonism());
        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
