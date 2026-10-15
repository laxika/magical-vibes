package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SkyclaveSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroneOfMakindi.class, SkyclaveSentinel.class, TazeemRoilmage.class})
class ThroneOfMakindiTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        readyThrone();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void putsChargeCounterOnItselfForOneMana() {
        Permanent throne = readyThrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(throne.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void addsTwoManaOfOneChosenColorWithChargeCounter() {
        Permanent throne = readyThrone();
        throne.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(throne.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void kickedOnlyManaCannotPayUnkickedSpellButPaysKickedSpell() {
        readyThrone().setCounterCount(CounterType.CHARGE, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(new SkyclaveSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyclave Sentinel");
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    void cannotActivateRestrictedManaAbilityWithoutChargeCounter() {
        Permanent throne = readyThrone();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(throne.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyManaTotal()).isZero();
    }

    @Test
    void chargingRequiresManaAndUsesTheStack() {
        Permanent throne = readyThrone();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(throne.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(throne.isTapped()).isTrue();
        assertThat(throne.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(throne.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void restrictedManaPaysColoredBaseCostOfKickedSpell() {
        Permanent throne = readyThrone();
        throne.setCounterCount(CounterType.CHARGE, 2);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(throne.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(throne.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new TazeemRoilmage()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazeem Roilmage");
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void restrictedManaCannotPayActivatedAbilityCost() {
        Permanent source = readyThrone();
        source.setCounterCount(CounterType.CHARGE, 1);
        Permanent otherThrone = harness.addToBattlefieldAndReturn(player1, new ThroneOfMakindi());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(otherThrone.isTapped()).isFalse();
        assertThat(otherThrone.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    private Permanent readyThrone() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfMakindi());
        throne.setSummoningSick(false);
        return throne;
    }
}
