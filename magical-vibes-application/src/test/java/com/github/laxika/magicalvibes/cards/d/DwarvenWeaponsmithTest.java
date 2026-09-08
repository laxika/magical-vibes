package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SunglassesOfUrza;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenWeaponsmith.class, SunglassesOfUrza.class, GrizzlyBears.class})
class DwarvenWeaponsmithTest extends BaseCardTest {

    @Test
    @DisplayName("During its controller's upkeep, tapping and sacrificing an artifact puts a +1/+1 counter on target creature")
    void putsCounterDuringOwnUpkeep() {
        Permanent weaponsmith = addCreatureReady(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new SunglassesOfUrza());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(weaponsmith.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Sunglasses of Urza");
    }

    @Test
    @DisplayName("Cannot be activated while tapped")
    void cannotActivateWhileTapped() {
        Permanent weaponsmith = addCreatureReady(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new SunglassesOfUrza());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        weaponsmith.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated outside its controller's upkeep")
    void cannotActivateOutsideOwnUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new SunglassesOfUrza());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot be activated during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new SunglassesOfUrza());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Requires an artifact to sacrifice")
    void requiresArtifactToSacrifice() {
        addCreatureReady(player1, new DwarvenWeaponsmith());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new DwarvenWeaponsmith());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SunglassesOfUrza());

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void putsCounterOnOpponentCreature() {
        Permanent weaponsmith = addCreatureReady(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new SunglassesOfUrza());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 0, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(weaponsmith.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Sunglasses of Urza");
    }
}
