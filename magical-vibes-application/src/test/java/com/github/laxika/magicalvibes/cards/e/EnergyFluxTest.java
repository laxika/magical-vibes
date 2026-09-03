package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnergyFlux.class, FountainOfYouth.class, GrizzlyBears.class})
class EnergyFluxTest extends BaseCardTest {

    private void addEnergyFlux(Player controller) {
        harness.addToBattlefield(controller, new EnergyFlux());
    }

    private Permanent addFountain(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new FountainOfYouth());
    }

    @Test
    @DisplayName("Declining to pay {2} sacrifices the artifact")
    void decliningPaymentSacrificesArtifact() {
        addEnergyFlux(player1);
        addFountain(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Paying {2} keeps the artifact on the battlefield")
    void payingKeepsArtifact() {
        addEnergyFlux(player1);
        addFountain(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices the artifact")
    void acceptingWithoutEnoughManaSacrificesArtifact() {
        addEnergyFlux(player1);
        addFountain(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Grant is global: an opponent's Energy Flux still taxes your artifact")
    void opponentsEnergyFluxTaxesYourArtifact() {
        addEnergyFlux(player2);
        addFountain(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger during your upkeep")
    void opponentArtifactNotTriggeredDuringYourUpkeep() {
        addEnergyFlux(player1);
        Permanent opponentArtifact = addFountain(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentArtifact.getId()));
    }

    @Test
    @DisplayName("Non-artifact permanents are unaffected")
    void nonArtifactUnaffected() {
        addEnergyFlux(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }
}
