package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraTorchOfDefiance;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethertorchRenegade.class, GrizzlyBears.class, ChandraTorchOfDefiance.class})
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

    @Test
    void secondAbilityDealsSixDamageToPlaneswalkerWithoutDamagingItsController() {
        addReadyRenegade();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTorchOfDefiance());
        target.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLife(player2, 20);
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void secondAbilityCanTargetItsController() {
        addReadyRenegade();
        harness.setLife(player1, 20);
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    void energyAndTapCostsArePaidBeforeDamageResolves() {
        Permanent renegade = addReadyRenegade();
        Permanent target = addCreatureReady(player2, new AethertorchRenegade());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(renegade.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void summoningSicknessPreventsBothTapAbilities() {
        Permanent renegade = harness.addToBattlefieldAndReturn(player1, new AethertorchRenegade());
        renegade.setSummoningSick(true);
        gd.playerEnergyCounters.put(player1.getId(), 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, renegade.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
        assertThat(renegade.isTapped()).isFalse();
    }

    @Test
    void tappedRenegadeCannotActivateEitherAbility() {
        Permanent renegade = addReadyRenegade();
        renegade.tap();
        gd.playerEnergyCounters.put(player1.getId(), 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, renegade.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
    }

    @Test
    void enteringAddsEnergyOnlyToItsController() {
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 2);

        harness.enterBattlefieldAndReturn(player2, new AethertorchRenegade());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(6);
    }

    private Permanent addReadyRenegade() {
        return addCreatureReady(player1, new AethertorchRenegade());
    }
}
