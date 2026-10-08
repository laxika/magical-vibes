package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoldarenAmbusher.class, CaptivatingVampire.class, GrizzlyBears.class, LilianaOfTheVeil.class})
class VoldarenAmbusherTest extends BaseCardTest {

    @Test
    void damagesPlaneswalker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        liliana.setCounterCount(CounterType.LOYALTY, 3);
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Liliana of the Veil");
    }

    @Test
    void countsVampiresAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void controllerLifeLossDoesNotEnableTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Voldaren Ambusher");
    }

    @Test
    @DisplayName("No ETB trigger when no opponent lost life this turn")
    void noTriggerWithoutLifeLoss() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Voldaren Ambusher");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals damage equal to Vampires controlled (counts itself)")
    void dealsDamageCountingItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities(); // resolve creature — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // resolve ETB

        // 1 Vampire (itself) → 1 damage to 2/2 — survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals more damage with additional Vampires")
    void dealsMoreDamageWithMultipleVampires() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        // Ambusher + Captivating Vampire = 2 → lethal
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts only your Vampires, not opponent's")
    void countsOnlyControllerVampires() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new CaptivatingVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        // Only Ambusher counts → 1 damage
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can decline up to one target")
    void canDeclineTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();
        // Choose yourself to decline
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Trigger goes on stack after target chosen")
    void triggerGoesOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Voldaren Ambusher");
    }

    @Test
    @DisplayName("Trigger prompt offers creatures and decline")
    void triggerPromptOffersCreaturesAndDecline() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new VoldarenAmbusher(), "{2}{R}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        UUID ambusherId = harness.getPermanentId(player1, "Voldaren Ambusher");
        assertThat(choice.validIds()).contains(bearsId, ambusherId, player1.getId());
    }
}
