package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PyreCharger.class)
class PyreChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Pyre Charger attack the turn it enters")
    void hasteAllowsImmediateAttack() {
        harness.setLife(player2, 20);
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new PyreCharger());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(charger)));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void pumpGivesPlusOnePlusZero() {
        Permanent charger = addCreatureReady(player1, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(1);
        assertThat(charger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly, stacking the boost")
    void pumpStacks() {
        Permanent charger = addCreatureReady(player1, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(charger.getPowerModifier()).isEqualTo(3);
        assertThat(charger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(charger.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Queued activations resolve separately and boost only their source")
    void queuedPumpsOnlyBoostTheirSource() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new PyreCharger());
        Permanent otherCharger = harness.addToBattlefieldAndReturn(player1, new PyreCharger());
        Permanent opposingCharger = harness.addToBattlefieldAndReturn(player2, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(charger.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(charger.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(2);
        assertThat(charger.getToughnessModifier()).isZero();
        assertThat(otherCharger.getPowerModifier()).isZero();
        assertThat(opposingCharger.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped Pyre Charger can activate its pump without untapping")
    void tappedChargerCanPump() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new PyreCharger());
        charger.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(1);
        assertThat(charger.getToughnessModifier()).isZero();
        assertThat(charger.isTapped()).isTrue();
    }
}
