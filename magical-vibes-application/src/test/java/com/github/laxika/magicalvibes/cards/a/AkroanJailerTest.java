package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyrantsMachine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkroanJailer.class, GrizzlyBears.class, TyrantsMachine.class})
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

    @Test
    @DisplayName("A summoning-sick jailer cannot pay the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent jailer = harness.addToBattlefieldAndReturn(player1, new AkroanJailer());
        jailer.setSummoningSick(true);
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jailer.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jailer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped jailer cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent jailer = addCreatureReady(player1, new AkroanJailer());
        jailer.setTapped(true);
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jailer.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The jailer can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent jailer = addCreatureReady(player1, new AkroanJailer());
        payMana();

        harness.activateAbility(player1, 0, null, jailer.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(jailer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature remains a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new AkroanJailer());
        Permanent target = addCreatureReady(player2, new AkroanJailer());
        target.setTapped(true);
        payMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The target is tapped on resolution rather than activation")
    void targetRemainsUntappedUntilResolution() {
        Permanent jailer = addCreatureReady(player1, new AkroanJailer());
        Permanent target = addCreatureReady(player2, new AkroanJailer());
        payMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(jailer.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Three generic mana cannot replace the white mana in the cost")
    void cannotActivateWithoutWhiteMana() {
        Permanent jailer = addCreatureReady(player1, new AkroanJailer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jailer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(jailer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves independently of its source")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent jailer = addCreatureReady(player1, new AkroanJailer());
        Permanent target = addCreatureReady(player2, new AkroanJailer());
        payMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(jailer);
        gd.playerGraveyards.get(player1.getId()).add(jailer.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
