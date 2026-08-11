package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VileConsumptionTest extends BaseCardTest {

    private void addVileConsumption(Player controller) {
        gd.playerBattlefields.get(controller.getId()).add(new Permanent(new VileConsumption()));
    }

    private Permanent addBears(Player controller) {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(controller.getId()).add(bears);
        return bears;
    }

    @Test
    @DisplayName("Declining to pay 1 life sacrifices the creature")
    void decliningPaymentSacrificesCreature() {
        addVileConsumption(player1);
        addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying 1 life keeps the creature on the battlefield")
    void payingKeepsCreature() {
        addVileConsumption(player1);
        addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Grant is global: an opponent's Vile Consumption still taxes your creature")
    void opponentsVileConsumptionTaxesYourCreature() {
        addVileConsumption(player2);
        addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's creature does not trigger during your upkeep")
    void opponentCreatureNotTriggeredDuringYourUpkeep() {
        addVileConsumption(player1);
        Permanent opponentBears = addBears(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentBears.getId()));
    }

    @Test
    @DisplayName("Non-creature permanents are unaffected")
    void nonCreatureUnaffected() {
        addVileConsumption(player1);
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(fountain);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(fountain.getId()));
    }
}
