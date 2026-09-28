package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifegift.class, GodsEyeGateToTheReikai.class, GnarledMass.class})
class LifegiftTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may gain 1 life when their land enters")
    void controllerMayGainLifeOnOwnLand() {
        addLifegift(player1);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GodsEyeGateToTheReikai()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller may gain 1 life when an opponent's land enters")
    void controllerMayGainLifeOnOpponentsLand() {
        addLifegift(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GodsEyeGateToTheReikai()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void decliningGainsNoLife() {
        addLifegift(player1);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GodsEyeGateToTheReikai()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A land entering without being played still triggers Lifegift")
    void landEnteringWithoutBeingPlayedTriggers() {
        addLifegift(player1);
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new GodsEyeGateToTheReikai());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("A nonland permanent entering does not trigger Lifegift")
    void nonlandDoesNotTrigger() {
        addLifegift(player1);
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        harness.assertLife(player1, 20);
    }

    private void addLifegift(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new Lifegift());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
