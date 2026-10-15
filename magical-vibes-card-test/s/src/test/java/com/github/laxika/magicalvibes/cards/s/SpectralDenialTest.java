package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralDenial.class, AirElemental.class, GrizzlyBears.class})
class SpectralDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each controlled creature with power 4 or greater")
    void costReductionCountsControlledHighPowerCreatures() {
        harness.addToBattlefield(player2, new AirElemental());

        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce its cost for a low-power creature or an opponent's creature")
    void costReductionIgnoresNonmatchingCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 2, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay for X=2");
    }

    @Test
    @DisplayName("Counters a spell when its controller cannot pay X")
    void countersWhenControllerCannotPayX() {
        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves a spell on the stack when its controller pays X")
    void doesNotCounterWhenControllerPaysX() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple discounts reduce casting cost without reducing the payment demanded")
    void multipleDiscountsPreserveChosenX() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a creature whose current power reaches four through counters")
    void costReductionUsesCurrentPower() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A creature reduced below four power no longer grants a discount")
    void costReductionIgnoresCreatureWhosePowerDropped() {
        var creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay for X=1");
    }

    @Test
    @DisplayName("Counters when the controller declines an affordable payment")
    void countersWhenControllerDeclinesPayment() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("At X zero the controller can pay zero with an empty mana pool")
    void zeroPaymentCanBeAccepted() {
        harness.addToBattlefield(player2, new AirElemental());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess discounts cannot remove the blue mana requirement")
    void costReductionCannotRemoveColoredCost() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller may decline a zero-mana payment and let the spell be countered")
    void zeroPaymentCanBeDeclined() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapped creatures with power four still reduce the casting cost")
    void tappedCreatureStillGrantsDiscount() {
        var creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        creature.tap();

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
