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

@CardUsed({AkroanMastiff.class, GrizzlyBears.class, TyrantsMachine.class})
class AkroanMastiffTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target creature")
    void resolvingTapsTargetCreature() {
        addCreatureReady(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWhiteMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps the mastiff")
    void activatingTapsMastiff() {
        Permanent mastiff = addCreatureReady(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWhiteMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(mastiff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature its controller controls")
    void canTapOwnCreature() {
        addCreatureReady(player1, new AkroanMastiff());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        addWhiteMana();

        harness.activateAbility(player1, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new AkroanMastiff());
        Permanent machine = harness.addToBattlefieldAndReturn(player2, new TyrantsMachine());
        addWhiteMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, machine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot pay the white activation cost with colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent mastiff = addCreatureReady(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new AkroanMastiff());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(mastiff.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mastiff = harness.addToBattlefieldAndReturn(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new AkroanMastiff());
        addWhiteMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(mastiff.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate an already tapped mastiff")
    void cannotActivateWhileTapped() {
        Permanent mastiff = addCreatureReady(player1, new AkroanMastiff());
        mastiff.tap();
        Permanent target = addCreatureReady(player2, new AkroanMastiff());
        addWhiteMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent mastiff = addCreatureReady(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new AkroanMastiff());
        target.tap();
        addWhiteMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(mastiff.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mastiff can target itself")
    void canTargetItself() {
        Permanent mastiff = addCreatureReady(player1, new AkroanMastiff());
        addWhiteMana();

        harness.activateAbility(player1, 0, null, mastiff.getId());
        harness.passBothPriorities();

        assertThat(mastiff.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent mastiff = addCreatureReady(player1, new AkroanMastiff());
        Permanent target = addCreatureReady(player2, new AkroanMastiff());
        addWhiteMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mastiff);
        gd.playerGraveyards.get(player1.getId()).add(mastiff.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void addWhiteMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
