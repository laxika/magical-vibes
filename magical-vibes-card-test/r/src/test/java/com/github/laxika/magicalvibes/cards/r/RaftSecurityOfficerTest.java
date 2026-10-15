package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaftSecurityOfficer.class, GrizzlyBears.class, AirElemental.class, Forest.class})
class RaftSecurityOfficerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one mana and taps a creature with power 3 or less")
    void reducesCostForLowPowerCreature() {
        addReadyOfficer();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Costs two mana for a creature with power greater than 3")
    void doesNotReduceCostForHighPowerCreature() {
        addReadyOfficer();
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyOfficer();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Uses current power and includes power exactly three in the discount")
    void reducesCostForCreatureWithCurrentPowerThree() {
        addReadyOfficer();
        Permanent target = addCreatureReady(player2, new RaftSecurityOfficer());
        target.setPowerModifier(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One mana cannot pay for a target whose current power exceeds three")
    void cannotUseDiscountWhenCurrentPowerExceedsThree() {
        Permanent officer = addCreatureReady(player1, new RaftSecurityOfficer());
        Permanent target = addCreatureReady(player2, new RaftSecurityOfficer());
        target.setPowerModifier(3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(officer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The discount still requires one mana")
    void cannotActivateWithoutMana() {
        Permanent officer = addCreatureReady(player1, new RaftSecurityOfficer());
        Permanent target = addCreatureReady(player2, new RaftSecurityOfficer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(officer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taps the officer as a cost before tapping the target on resolution")
    void paysTapCostBeforeResolutionAndDoesNotRecheckPowerForCost() {
        Permanent officer = addCreatureReady(player1, new RaftSecurityOfficer());
        Permanent target = addCreatureReady(player2, new RaftSecurityOfficer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(officer.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        target.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick officer cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RaftSecurityOfficer());
        Permanent target = addCreatureReady(player2, new RaftSecurityOfficer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent officer = addCreatureReady(player1, new RaftSecurityOfficer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, officer.getId());
        harness.passBothPriorities();

        assertThat(officer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyOfficer() {
        addCreatureReady(player1, new RaftSecurityOfficer());
    }
}
