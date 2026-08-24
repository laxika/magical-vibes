package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenWeaponsmith.class, TormodsCrypt.class, GrizzlyBears.class})
class DwarvenWeaponsmithTest extends BaseCardTest {

    @Test
    @DisplayName("During its controller's upkeep, tapping and sacrificing an artifact puts a +1/+1 counter on target creature")
    void putsCounterDuringOwnUpkeep() {
        Permanent weaponsmith = harness.addToBattlefieldAndReturn(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        weaponsmith.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(weaponsmith.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Cannot be activated outside its controller's upkeep")
    void cannotActivateOutsideOwnUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new DwarvenWeaponsmith());
        harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
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
        harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Requires an artifact to sacrifice")
    void requiresArtifactToSacrifice() {
        Permanent weaponsmith = harness.addToBattlefieldAndReturn(player1, new DwarvenWeaponsmith());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        weaponsmith.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        Permanent weaponsmith = harness.addToBattlefieldAndReturn(player1, new DwarvenWeaponsmith());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        weaponsmith.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
