package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.r.RootSnare;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DovinsAcuity.class, RootSnare.class, SauroformHybrid.class})
class DovinsAcuityTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, you gain 2 life and draw a card")
    void entersGainsLifeAndDrawsCard() {
        SauroformHybrid drawn = new SauroformHybrid();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new DovinsAcuity(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("May return to hand when you cast an instant during your main phase")
    void mayReturnToHandForInstantDuringMainPhase() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RootSnare(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dovin's Acuity");
        harness.assertNotOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Does not return when the instant is cast outside your main phase")
    void doesNotReturnForInstantOutsideMainPhase() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new RootSnare(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Does not return when you cast a creature during your main phase")
    void doesNotReturnForCreatureSpell() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new SauroformHybrid(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Returning Dovin's Acuity is optional")
    void mayDeclineReturnToHand() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.castFromHand(player1, new RootSnare(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dovin's Acuity");
        harness.assertNotInHand(player1, "Dovin's Acuity");
        harness.assertInGraveyard(player1, "Root Snare");
    }

    @Test
    @DisplayName("An instant during your second main phase also triggers the return")
    void mayReturnDuringPostcombatMainPhase() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new RootSnare(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dovin's Acuity");
        harness.assertNotOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Your instant during an opponent's main phase does not trigger")
    void doesNotTriggerDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RootSnare(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Root Snare");
        harness.assertOnBattlefield(player1, "Dovin's Acuity");
        harness.assertNotInHand(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("An opponent's instant during your main phase does not trigger")
    void doesNotTriggerForOpponentsInstant() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.castFromHand(player2, new RootSnare(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Root Snare");
        harness.assertOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Entering without being cast still gains life and draws a card")
    void enteringWithoutCastingTriggersAbility() {
        harness.setHand(player1, List.of());
        SauroformHybrid drawn = new SauroformHybrid();
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new DovinsAcuity());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
