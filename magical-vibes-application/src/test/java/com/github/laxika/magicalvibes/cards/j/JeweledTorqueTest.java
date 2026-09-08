package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JeweledTorqueTest extends BaseCardTest {

    private static Card createCreature(String name, List<CardColor> colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColors(colors);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Choosing a color as Jeweled Torque enters stores that color")
    void choosesColorOnEntry() {
        harness.setHand(player1, List.of(new JeweledTorque()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class))
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
        harness.setHand(player2, List.of(createCreature("Green Creature", List.of(CardColor.GREEN))));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Declining the payment produces no life gain")
    void declinesPayment() {
        harness.addToBattlefield(player1, new JeweledTorque());
        Permanent torque = findPermanent(player1, "Jeweled Torque");
        torque.setChosenColor(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Green Creature", List.of(CardColor.GREEN))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A spell of another color does not trigger Jeweled Torque")
    void doesNotTriggerForAnotherColor() {
        harness.addToBattlefield(player1, new JeweledTorque());
        Permanent torque = findPermanent(player1, "Jeweled Torque");
        torque.setChosenColor(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Red Creature", List.of(CardColor.RED))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
