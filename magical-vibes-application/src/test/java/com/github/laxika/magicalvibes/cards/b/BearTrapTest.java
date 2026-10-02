package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BearTrap.class, GrizzlyBears.class, SerraAngel.class})
class BearTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target creature")
    void sacrificesItselfAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new BearTrap());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Bear Trap");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal damage destroys the target creature")
    void lethalDamageDestroysTargetCreature() {
        harness.addToBattlefield(player1, new BearTrap());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new BearTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, trap.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void castsDuringOpponentsEndStep() {
        gd.activePlayerId = player2.getId();
        gd.currentStep = TurnStep.END_STEP;

        harness.castFromHand(player1, new BearTrap(), "{1}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bear Trap");
    }

    @Test
    @DisplayName("A newly entered noncreature trap can tap to damage its controller's creature")
    void newlyEnteredTrapCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new BearTrap());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Bear Trap");
        harness.assertInGraveyard(player1, "Bear Trap");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped trap cannot activate its tap ability")
    void tappedTrapCannotActivate() {
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new BearTrap());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        trap.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Bear Trap");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating requires all three mana")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new BearTrap());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bear Trap");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
