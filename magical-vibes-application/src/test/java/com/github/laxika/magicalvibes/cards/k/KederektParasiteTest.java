package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KederektParasiteTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Opponent draw with a red permanent — accepting the may deals 1 damage to that player")
    void acceptingMayDealsOneDamageToDrawingOpponent() {
        harness.addToBattlefield(player1, new KederektParasite());
        harness.addToBattlefield(player1, new HillGiant()); // red permanent enables the trigger
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the may deals no damage")
    void decliningMayDealsNoDamage() {
        harness.addToBattlefield(player1, new KederektParasite());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the trigger → may prompt

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Without a red permanent the ability does not trigger")
    void noRedPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new KederektParasite());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green, not red
        harness.setLife(player2, 20);

        advanceToDraw(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller's own draw does not trigger the ability")
    void controllerDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new KederektParasite());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLife(player1, 20);

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
