package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleetingAven.class, Censor.class, Distress.class, GrizzlyBears.class})
class FleetingAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card returns Fleeting Aven to its owner's hand")
    void cyclingReturnsFleetingAvenToHand() {
        harness.addToBattlefield(player1, new FleetingAven());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Fleeting Aven");
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent cycling a card also returns Fleeting Aven")
    void opponentCyclingReturnsFleetingAvenToHand() {
        harness.addToBattlefield(player1, new FleetingAven());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Fleeting Aven");
        harness.assertInGraveyard(player2, "Censor");
    }

    @Test
    @DisplayName("An ordinary discard does not return Fleeting Aven")
    void ordinaryDiscardDoesNotReturnFleetingAven() {
        harness.addToBattlefield(player1, new FleetingAven());
        harness.setHand(player1, new ArrayList<>(List.of(new Distress())));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Fleeting Aven");
    }
}
