package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToothAndClawTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing exactly two creatures creates a 3/1 Carnivore")
    void createsCarnivore() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");

        Permanent carnivore = findPermanent(player1, "Carnivore");
        assertThat(carnivore.getEffectivePower()).isEqualTo(3);
        assertThat(carnivore.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two creatures")
    void cannotActivateWithOneCreature() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("With more than two creatures the controller chooses which two to sacrifice")
    void choosesWhichCreaturesToSacrifice() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, elvesId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Carnivore");
    }

    @Test
    @DisplayName("Opponent's creatures cannot pay the sacrifice cost")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }
}
