package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToothAndClaw.class, BayouDragonfly.class, LowlandGiant.class})
class ToothAndClawTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing exactly two creatures creates a 3/1 Carnivore")
    void createsCarnivore() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new LowlandGiant());
        harness.addToBattlefield(player1, new BayouDragonfly());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Bayou Dragonfly");
        harness.assertOnBattlefield(player1, "Tooth and Claw");

        Permanent carnivore = findPermanent(player1, "Carnivore");
        assertThat(carnivore.getEffectivePower()).isEqualTo(3);
        assertThat(carnivore.getEffectiveToughness()).isEqualTo(1);
        assertThat(carnivore.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(carnivore.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two creatures")
    void cannotActivateWithOneCreature() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new LowlandGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("With more than two creatures the controller chooses which two to sacrifice")
    void choosesWhichCreaturesToSacrifice() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new LowlandGiant());
        harness.addToBattlefield(player1, new BayouDragonfly());
        harness.addToBattlefield(player1, new LowlandGiant());

        UUID dragonflyId = findPermanent(player1, "Bayou Dragonfly").getId();
        UUID giantId = findPermanent(player1, "Lowland Giant").getId();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, dragonflyId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, giantId);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bayou Dragonfly");
        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.assertOnBattlefield(player1, "Carnivore");
    }

    @Test
    @DisplayName("Opponent's creatures cannot pay the sacrifice cost")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new ToothAndClaw());
        harness.addToBattlefield(player1, new LowlandGiant());
        harness.addToBattlefield(player2, new LowlandGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }
}
