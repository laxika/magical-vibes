package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileConsumption.class, AncientKavu.class, Forest.class})
class VileConsumptionTest extends BaseCardTest {

    private void addVileConsumption(Player controller) {
        harness.addToBattlefield(controller, new VileConsumption());
    }

    private Permanent addAncientKavu(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new AncientKavu());
    }

    @Test
    @DisplayName("Declining to pay 1 life sacrifices the creature")
    void decliningPaymentSacrificesCreature() {
        addVileConsumption(player1);
        addAncientKavu(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertInGraveyard(player1, "Ancient Kavu");
    }

    @Test
    @DisplayName("Paying 1 life keeps the creature on the battlefield")
    void payingKeepsCreature() {
        addVileConsumption(player1);
        addAncientKavu(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ancient Kavu");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Grant is global: an opponent's Vile Consumption still taxes your creature")
    void opponentsVileConsumptionTaxesYourCreature() {
        addVileConsumption(player2);
        addAncientKavu(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
    }

    @Test
    @DisplayName("An opponent's creature does not trigger during your upkeep")
    void opponentCreatureNotTriggeredDuringYourUpkeep() {
        addVileConsumption(player1);
        Permanent opponentKavu = addAncientKavu(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentKavu.getId()));
    }

    @Test
    @DisplayName("Non-creature permanents are unaffected")
    void nonCreatureUnaffected() {
        addVileConsumption(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
    }

    @Test
    @DisplayName("Each creature receives its own upkeep trigger")
    void eachCreatureReceivesItsOwnUpkeepTrigger() {
        addVileConsumption(player1);
        addAncientKavu(player1);
        addAncientKavu(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Ancient Kavu"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Ancient Kavu"))
                .hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Ancient Kavu"))
                .hasSize(2);
    }
}
