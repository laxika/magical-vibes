package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.k.KyrenToy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnergyFlux.class, KyrenToy.class, DeadlyInsect.class})
class EnergyFluxTest extends BaseCardTest {

    private void addEnergyFlux(Player controller) {
        harness.addToBattlefield(controller, new EnergyFlux());
    }

    private Permanent addKyrenToy(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new KyrenToy());
    }

    private Permanent addDeadlyInsect(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new DeadlyInsect());
    }

    @Test
    @DisplayName("Declining to pay {2} sacrifices the artifact")
    void decliningPaymentSacrificesArtifact() {
        addEnergyFlux(player1);
        addKyrenToy(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Kyren Toy");
        harness.assertInGraveyard(player1, "Kyren Toy");
    }

    @Test
    @DisplayName("Paying {2} keeps the artifact on the battlefield")
    void payingKeepsArtifact() {
        addEnergyFlux(player1);
        addKyrenToy(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Kyren Toy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices the artifact")
    void acceptingWithoutEnoughManaSacrificesArtifact() {
        addEnergyFlux(player1);
        addKyrenToy(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Kyren Toy");
        harness.assertInGraveyard(player1, "Kyren Toy");
    }

    @Test
    @DisplayName("Grant is global: an opponent's Energy Flux still taxes your artifact")
    void opponentsEnergyFluxTaxesYourArtifact() {
        addEnergyFlux(player2);
        addKyrenToy(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Kyren Toy");
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger during your upkeep")
    void opponentArtifactNotTriggeredDuringYourUpkeep() {
        addEnergyFlux(player1);
        Permanent opponentArtifact = addKyrenToy(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentArtifact.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Non-artifact permanents are unaffected")
    void nonArtifactUnaffected() {
        addEnergyFlux(player1);
        Permanent insect = addDeadlyInsect(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(insect.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact triggers during its controller's upkeep")
    void artifactTriggersDuringItsControllersUpkeep() {
        addEnergyFlux(player1);
        addKyrenToy(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Kyren Toy");
        harness.assertInGraveyard(player2, "Kyren Toy");
    }

    @Test
    @DisplayName("Each artifact receives its own upkeep trigger")
    void eachArtifactReceivesItsOwnUpkeepTrigger() {
        addEnergyFlux(player1);
        Permanent firstArtifact = addKyrenToy(player1);
        Permanent secondArtifact = addKyrenToy(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstArtifact, secondArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Kyren Toy"))
                .hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
