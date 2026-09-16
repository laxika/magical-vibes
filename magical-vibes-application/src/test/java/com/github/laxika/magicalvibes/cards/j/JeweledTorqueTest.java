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
        harness.addToBattlefield(player1, new JeweledTorque());
        Permanent torque = findPermanent(player1, "Jeweled Torque");
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
        harness.addToBattlefield(player1, new JeweledTorque());
        Permanent torque = findPermanent(player1, "Jeweled Torque");
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
        harness.addToBattlefield(player1, new JeweledTorque());
        Permanent torque = findPermanent(player1, "Jeweled Torque");
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
        harness.addToBattlefield(player1, new JeweledTorque());
        Permanent torque = findPermanent(player1, "Jeweled Torque");
        torque.setChosenColor(CardColor.GREEN);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new DeepwoodWolverine(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore);
    }
}
