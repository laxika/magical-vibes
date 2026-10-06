package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MireBoa;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RathiTrapper.class, MireBoa.class, UrborgTombOfYawgmoth.class})
class RathiTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Black mana and tapping Rathi Trapper taps target creature")
    void tapsTargetCreature() {
        Permanent trapper = addCreatureReady(player1, new RathiTrapper());
        Permanent target = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires black mana")
    void requiresBlackMana() {
        Permanent trapper = addCreatureReady(player1, new RathiTrapper());
        Permanent target = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(trapper.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new RathiTrapper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Rathi Trapper cannot pay the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent trapper = harness.addToBattlefieldAndReturn(player1, new RathiTrapper());
        Permanent target = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(trapper.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already-tapped Rathi Trapper cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent trapper = addCreatureReady(player1, new RathiTrapper());
        trapper.tap();
        Permanent target = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rathi Trapper can target itself despite tapping to pay its cost")
    void canTargetItself() {
        Permanent trapper = addCreatureReady(player1, new RathiTrapper());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, trapper.getId());
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already-tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent trapper = addCreatureReady(player1, new RathiTrapper());
        Permanent target = addCreatureReady(player1, new MireBoa());
        target.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
