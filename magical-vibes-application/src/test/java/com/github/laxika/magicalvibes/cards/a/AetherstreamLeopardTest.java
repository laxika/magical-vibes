package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherstreamLeopardTest extends BaseCardTest {

    @Test
    void entersWithOneEnergyCounter() {
        harness.setHand(player1, List.of(new AetherstreamLeopard()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayPayEnergyOnAttackForBoost() {
        Permanent leopard = addCreatureReady(player1, new AetherstreamLeopard());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(3);
    }

    @Test
    void decliningEnergyPaymentDoesNothing() {
        Permanent leopard = addCreatureReady(player1, new AetherstreamLeopard());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(3);
    }

    @Test
    void cannotGetBoostWithoutEnergy() {
        Permanent leopard = addCreatureReady(player1, new AetherstreamLeopard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(3);
    }
}
