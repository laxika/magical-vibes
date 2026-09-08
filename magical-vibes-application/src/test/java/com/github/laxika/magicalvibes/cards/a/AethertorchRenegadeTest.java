package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AethertorchRenegadeTest extends BaseCardTest {

    @Test
    void entersWithFourEnergyCounters() {
        harness.setHand(player1, List.of(new AethertorchRenegade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void paysTwoEnergyAndTapsToDealOneDamageToCreature() {
        Permanent renegade = addReadyRenegade();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(renegade.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void paysEightEnergyAndTapsToDealSixDamageToPlayer() {
        Permanent renegade = addReadyRenegade();
        harness.setLife(player2, 20);
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(renegade.isTapped()).isTrue();
        harness.assertLife(player2, 14);
    }

    @Test
    void cannotActivateFirstAbilityWithoutTwoEnergyCounters() {
        addReadyRenegade();
        gd.playerEnergyCounters.put(player1.getId(), 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
    }

    @Test
    void cannotActivateSecondAbilityWithoutEightEnergyCounters() {
        addReadyRenegade();
        gd.playerEnergyCounters.put(player1.getId(), 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");
    }

    @Test
    void firstAbilityCannotTargetAPlayer() {
        addReadyRenegade();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAbilityCannotTargetAcreature() {
        addReadyRenegade();
        gd.playerEnergyCounters.put(player1.getId(), 8);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRenegade() {
        return addCreatureReady(player1, new AethertorchRenegade());
    }
}
