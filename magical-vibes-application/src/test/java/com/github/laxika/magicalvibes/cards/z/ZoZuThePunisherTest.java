package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BlindWithAnger;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZoZuThePunisher.class, DryadArbor.class, BlindWithAnger.class, WanderingOnes.class})
class ZoZuThePunisherTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's land entering deals 2 damage to that opponent")
    void opponentLandDamagesOpponent() {
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DryadArbor()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller's own land entering deals 2 damage to the controller")
    void ownLandDamagesController() {
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DryadArbor()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A non-land permanent entering does not trigger Zo-Zu")
    void nonLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new WanderingOnes()));
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Uses a land's current controller when an own land changes control before resolution")
    void ownLandControllerChangeBeforeResolution() {
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DryadArbor()));
        harness.setHand(player2, List.of(new BlindWithAnger()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.playLand(player1, 0);

        UUID dryadId = harness.getPermanentId(player1, "Dryad Arbor");
        assertThat(gd.stack).hasSize(1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, dryadId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Uses a land's current controller when an opponent's land changes control before resolution")
    void opponentLandControllerChangeBeforeResolution() {
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DryadArbor()));
        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.playLand(player2, 0);

        UUID dryadId = harness.getPermanentId(player2, "Dryad Arbor");
        assertThat(gd.stack).hasSize(1);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, dryadId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
