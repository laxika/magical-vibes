package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipwreckMorayTest extends BaseCardTest {

    @Test
    void entersWithFourEnergyCounters() {
        harness.setHand(player1, List.of(new ShipwreckMoray()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void paysEnergyToGetPlusTwoMinusTwoUntilEndOfTurn() {
        Permanent moray = addCreatureReady(player1, new ShipwreckMoray());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, moray)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moray)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moray)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, moray)).isEqualTo(5);
    }

    @Test
    void cannotActivateWithoutEnergy() {
        Permanent moray = addCreatureReady(player1, new ShipwreckMoray());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
        assertThat(gqs.getEffectivePower(gd, moray)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, moray)).isEqualTo(5);
    }
}
