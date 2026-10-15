package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CheckpointOfficer.class, Forest.class})
class CheckpointOfficerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature")
    void tapsTargetCreature() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(officer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(officer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can tap another creature its controller controls")
    void canTapOwnCreature() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player1, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(officer.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, officer.getId());
        harness.passBothPriorities();

        assertThat(officer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new CheckpointOfficer());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(officer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent officer = harness.addToBattlefieldAndReturn(player1, new CheckpointOfficer());
        officer.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(officer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        officer.tap();
        Permanent target = addCreatureReady(player2, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the activation cost without white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(officer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with only one white mana")
    void cannotActivateWithoutGenericMana() {
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new CheckpointOfficer());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(officer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
