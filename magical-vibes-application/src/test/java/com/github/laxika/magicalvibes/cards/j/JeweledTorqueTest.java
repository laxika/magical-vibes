package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DeepwoodWolverine;
import com.github.laxika.magicalvibes.cards.f.FlailingSoldier;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeweledTorque.class, DeepwoodWolverine.class, FlailingSoldier.class})
class JeweledTorqueTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color as Jeweled Torque enters stores that color")
    void choosesColorOnEntry() {
        harness.castFromHand(player1, new JeweledTorque(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Jeweled Torque").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("An opponent's spell of the chosen color lets the controller pay {2} to gain 2 life")
    void opponentCastsChosenColorSpellAndControllerPays() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castFromHand(player2, new DeepwoodWolverine(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Declining the payment produces no life gain")
    void declinesPayment() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DeepwoodWolverine(), "{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("A spell of another color does not trigger Jeweled Torque")
    void doesNotTriggerForAnotherColor() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);

        harness.castFromHand(player1, new FlailingSoldier(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Flailing Soldier");
    }

    @Test
    @DisplayName("Accepting the trigger without {2} does not gain life")
    void cannotPayOptionalCost() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new DeepwoodWolverine(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("The cast trigger resolves before the spell and gains life during the payment resolution")
    void triggerResolvesBeforeSpell() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.RED);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new FlailingSoldier(), "{R}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Flailing Soldier");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Flailing Soldier");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flailing Soldier");
    }

    @Test
    @DisplayName("Declining payment still leaves the triggering spell unresolved")
    void decliningPaymentDoesNotResolveSpell() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DeepwoodWolverine(), "{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Deepwood Wolverine");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deepwood Wolverine");
    }

    @Test
    @DisplayName("A colorless artifact spell does not trigger Jeweled Torque")
    void doesNotTriggerForColorlessSpell() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new JeweledTorque(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Jeweled Torque")).hasSize(2);
        harness.assertLife(player1, lifeBefore);
    }
}
