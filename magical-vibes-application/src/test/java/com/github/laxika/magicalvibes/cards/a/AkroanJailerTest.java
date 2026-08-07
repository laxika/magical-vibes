package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyrantsMachine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AkroanJailerTest extends BaseCardTest {

    private void payMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Resolving the ability taps the target creature")
    void resolvingTapsTargetCreature() {
        addCreatureReady(player1, new AkroanJailer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        payMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps the jailer")
    void activatingTapsJailer() {
        Permanent jailer = addCreatureReady(player1, new AkroanJailer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        payMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(jailer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature its controller controls")
    void canTapOwnCreature() {
        addCreatureReady(player1, new AkroanJailer());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        payMana();

        harness.activateAbility(player1, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new AkroanJailer());
        Permanent machine = harness.addToBattlefieldAndReturn(player2, new TyrantsMachine());
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, machine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AkroanJailer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
