package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VivienMonstersAdvocate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpringjawTrap.class, GrizzlyBears.class, VivienMonstersAdvocate.class})
class SpringjawTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target creature")
    void sacrificesItselfAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new SpringjawTrap());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target player")
    void sacrificesItselfAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new SpringjawTrap());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's upkeep and activating immediately")
    void canCastAndActivateOnOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.castFromHand(player1, new SpringjawTrap(), "{1}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Springjaw Trap");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals three damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new SpringjawTrap());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new VivienMonstersAdvocate());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.assertInGraveyard(player2, "Vivien, Monsters' Advocate");
    }

    @Test
    @DisplayName("Any target includes its controller")
    void canDamageItsController() {
        harness.addToBattlefield(player1, new SpringjawTrap());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Springjaw Trap");
    }

    @Test
    @DisplayName("Insufficient mana leaves the Trap untapped and on the battlefield")
    void insufficientManaDoesNotPayCosts() {
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new SpringjawTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trap.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Springjaw Trap");
        harness.assertNotInGraveyard(player1, "Springjaw Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Trap cannot pay its tap cost")
    void cannotActivateWhenTapped() {
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new SpringjawTrap());
        trap.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Springjaw Trap");
        harness.assertNotInGraveyard(player1, "Springjaw Trap");
        assertThat(gd.stack).isEmpty();
    }
}
