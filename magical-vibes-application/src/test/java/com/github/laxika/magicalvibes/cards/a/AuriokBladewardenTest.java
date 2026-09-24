package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokBladewarden.class, AlphaMyr.class, AncientDen.class})
class AuriokBladewardenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping gives a target creature +X/+X equal to Auriok Bladewarden's current power")
    void tappingBoostsByCurrentPower() {
        Permanent bladewarden = addReadyBladewarden(player1);
        bladewarden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(bladewarden.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost uses Auriok Bladewarden's power when the ability resolves")
    void usesPowerAtResolution() {
        Permanent bladewarden = addReadyBladewarden(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        bladewarden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        addReadyBladewarden(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentCreature() {
        addReadyBladewarden(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can target a creature you control")
    void canTargetOwnCreature() {
        addReadyBladewarden(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost uses Auriok Bladewarden's last known power if it leaves before resolution")
    void usesLastKnownPowerIfSourceLeaves() {
        Permanent bladewarden = addReadyBladewarden(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bladewarden);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyBladewarden(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AncientDen());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBladewarden(Player player) {
        return addCreatureReady(player, new AuriokBladewarden());
    }
}
