package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        GameData gameData = harness.getGameData();
        Permanent perm = new Permanent(new ElectrostaticPummeler());
        perm.setSummoningSick(false);
        gameData.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
