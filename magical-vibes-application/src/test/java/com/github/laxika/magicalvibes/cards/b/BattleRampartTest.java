package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleRampart.class, FreshVolunteers.class, Mountain.class})
class BattleRampartTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to give a target creature haste")
    void grantsHasteToTargetCreature() {
        Permanent rampart = addReadyBattleRampart();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(rampart.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        addReadyBattleRampart();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        addReadyBattleRampart();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        addReadyBattleRampart();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBattleRampart() {
        return addCreatureReady(player1, new BattleRampart());
    }

    @Test
    @DisplayName("Can target itself without untapping itself")
    void canTargetItself() {
        Permanent rampart = addReadyBattleRampart();

        harness.activateAbility(player1, 0, null, rampart.getId());
        assertThat(rampart.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, rampart, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rampart, Keyword.HASTE)).isTrue();
        assertThat(rampart.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent rampart = harness.addToBattlefieldAndReturn(player1, new BattleRampart());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rampart.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(rampart.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        Permanent rampart = addReadyBattleRampart();
        harness.activateAbility(player1, 0, null, rampart.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rampart.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rampart.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted haste lets a newly entered Battle Rampart pay its tap cost")
    void grantedHasteAllowsTapAbilityDespiteSummoningSickness() {
        Permanent readyRampart = addReadyBattleRampart();
        Permanent newRampart = harness.addToBattlefieldAndReturn(player1, new BattleRampart());

        harness.activateAbility(player1, 0, null, newRampart.getId());
        harness.passBothPriorities();
        assertThat(newRampart.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, newRampart, Keyword.HASTE)).isTrue();

        harness.activateAbility(player1, 1, null, readyRampart.getId());
        harness.passBothPriorities();

        assertThat(newRampart.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, readyRampart, Keyword.HASTE)).isTrue();
    }
}
