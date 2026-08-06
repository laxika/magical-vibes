package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TyrantsMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target creature")
    void resolvingTapsTargetCreature() {
        harness.addToBattlefieldAndReturn(player1, new TyrantsMachine());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps the machine")
    void activatingTapsMachine() {
        Permanent machine = harness.addToBattlefieldAndReturn(player1, new TyrantsMachine());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(machine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature its controller controls")
    void canTapOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new TyrantsMachine());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefieldAndReturn(player1, new TyrantsMachine());
        Permanent otherMachine = harness.addToBattlefieldAndReturn(player2, new TyrantsMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherMachine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new TyrantsMachine());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
