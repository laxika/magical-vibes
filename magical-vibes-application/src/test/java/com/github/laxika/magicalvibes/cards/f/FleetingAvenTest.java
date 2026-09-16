package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Backslide;
import com.github.laxika.magicalvibes.cards.b.Blackmail;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleetingAven.class, Backslide.class, Blackmail.class, GlorySeeker.class})
class FleetingAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card returns Fleeting Aven to its owner's hand")
    void cyclingReturnsFleetingAvenToHand() {
        harness.addToBattlefield(player1, new FleetingAven());
        harness.setHand(player1, List.of(new Backslide()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        harness.assertInHand(player1, "Fleeting Aven");
        harness.assertInGraveyard(player1, "Backslide");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("An opponent cycling a card also returns Fleeting Aven")
    void opponentCyclingReturnsFleetingAvenToHand() {
        harness.addToBattlefield(player1, new FleetingAven());
        harness.setHand(player2, List.of(new Backslide()));
        harness.setLibrary(player2, List.of(new GlorySeeker()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        harness.assertInHand(player1, "Fleeting Aven");
        harness.assertInGraveyard(player2, "Backslide");
        harness.assertInHand(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("An ordinary discard does not return Fleeting Aven")
    void ordinaryDiscardDoesNotReturnFleetingAven() {
        harness.addToBattlefield(player1, new FleetingAven());
        harness.setHand(player1, List.of(new Blackmail()));
        harness.setHand(player2, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Fleeting Aven");
    }

    @Test
    @DisplayName("A cycling trigger does not return Fleeting Aven after it leaves the battlefield")
    void cyclingTriggerDoesNotReturnFleetingAvenAfterItLeavesBattlefield() {
        Permanent fleetingAven = addCreatureReady(player1, new FleetingAven());
        harness.setHand(player1, List.of(new Backslide()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fleetingAven));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fleeting Aven");
        harness.assertInGraveyard(player1, "Fleeting Aven");
        harness.assertInGraveyard(player1, "Backslide");
        harness.assertInHand(player1, "Glory Seeker");
        harness.assertNotInHand(player1, "Fleeting Aven");
    }
}
