package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.o.ONaginata;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KatakiWarsWage.class, ONaginata.class})
class KatakiWarsWageTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} keeps your artifact on the battlefield")
    void paysToKeepArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ONaginata());
        harness.addToBattlefield(player1, new KatakiWarsWage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Declining the payment sacrifices your artifact but not Kataki")
    void declinesAndSacrificesArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ONaginata());
        harness.addToBattlefield(player1, new KatakiWarsWage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "O-Naginata");
        harness.assertOnBattlefield(player1, "Kataki, War's Wage");
    }

    @Test
    @DisplayName("Each artifact gets its own upkeep payment")
    void eachArtifactGetsItsOwnUpkeepPayment() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new ONaginata());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new ONaginata());
        harness.addToBattlefield(player1, new KatakiWarsWage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstArtifact, secondArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("O-Naginata"))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Kataki, War's Wage");
    }

    @Test
    @DisplayName("Kataki affects artifacts controlled by an opponent")
    void affectsOpponentsArtifact() {
        harness.addToBattlefield(player1, new KatakiWarsWage());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ONaginata());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player2, "O-Naginata");
    }

    @Test
    @DisplayName("An artifact is sacrificed when its controller cannot pay")
    void cannotPayAndSacrificesArtifact() {
        harness.addToBattlefield(player1, new KatakiWarsWage());
        harness.addToBattlefield(player1, new ONaginata());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "O-Naginata");
        harness.assertInGraveyard(player1, "O-Naginata");
        harness.assertOnBattlefield(player1, "Kataki, War's Wage");
    }

    @Test
    @DisplayName("Two Katakis require two separate payments for the same artifact")
    void twoKatakisRequireTwoPayments() {
        harness.addToBattlefield(player1, new KatakiWarsWage());
        harness.addToBattlefield(player2, new KatakiWarsWage());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ONaginata());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "O-Naginata");
    }

    @Test
    @DisplayName("Only the active player's artifacts trigger during upkeep")
    void doesNotChargeInactivePlayersArtifacts() {
        harness.addToBattlefield(player1, new KatakiWarsWage());
        harness.addToBattlefield(player1, new ONaginata());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new ONaginata());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "O-Naginata");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifacts entering after upkeep begins do not trigger retroactively")
    void artifactEnteringDuringUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new KatakiWarsWage());

        advanceToUpkeep(player1);
        harness.enterBattlefieldAndReturn(player1, new ONaginata());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "O-Naginata");
    }
}
