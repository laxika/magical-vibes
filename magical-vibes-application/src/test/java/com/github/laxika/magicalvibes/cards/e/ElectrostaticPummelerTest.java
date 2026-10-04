package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElectrostaticPummeler.class})
class ElectrostaticPummelerTest extends BaseCardTest {

    @Test
    void entersWithThreeEnergyCounters() {
        harness.setHand(player1, java.util.List.of(new ElectrostaticPummeler()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysEnergyToDoublePowerAndToughness() {
        Permanent pummeler = addReadyPummeler(player1);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(pummeler.getEffectivePower()).isEqualTo(2);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void activatingTwiceCompoundsTheBoost() {
        Permanent pummeler = addReadyPummeler(player1);
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pummeler.getEffectivePower()).isEqualTo(4);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent pummeler = addReadyPummeler(player1);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pummeler.getEffectivePower()).isEqualTo(1);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutEnoughEnergy() {
        addReadyPummeler(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    private Permanent addReadyPummeler(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ElectrostaticPummeler());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void stackedActivationsPayImmediatelyAndUsePowerAtEachResolution() {
        Permanent pummeler = addReadyPummeler(player1);
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(pummeler.getEffectivePower()).isEqualTo(1);

        resolveAllTriggers();

        assertThat(pummeler.getEffectivePower()).isEqualTo(4);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void bothBonusesUsePowerRatherThanDoublingToughness() {
        Permanent pummeler = addReadyPummeler(player1);
        pummeler.setPowerModifier(2);
        pummeler.setToughnessModifier(4);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pummeler.getEffectivePower()).isEqualTo(6);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    void negativePowerGivesNoBonus() {
        Permanent pummeler = addReadyPummeler(player1);
        pummeler.setPowerModifier(-2);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pummeler.getEffectivePower()).isEqualTo(-1);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void canActivateWhileSummoningSickAndTapped() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player1, new ElectrostaticPummeler());
        pummeler.setSummoningSick(true);
        pummeler.tap();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pummeler.getEffectivePower()).isEqualTo(2);
        assertThat(pummeler.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void entryAddsEnergyToItsControllerExistingTotal() {
        gd.playerEnergyCounters.put(player2.getId(), 2);
        harness.enterBattlefieldAndReturn(player2, new ElectrostaticPummeler());

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }
}
