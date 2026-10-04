package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LionSash;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenMauler.class, GrizzlyBears.class, LeoninScimitar.class,
        LionSash.class, LoxodonWarhammer.class})
class DwarvenMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip abilities targeting Dwarven Mauler cost {2} less")
    void equipmentAbilitiesTargetingDwarvenMaulerCostTwoLess() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new DwarvenMauler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                mauler.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(mauler.getId());
    }

    @Test
    @DisplayName("Equip abilities targeting another creature are not reduced")
    void equipmentAbilitiesTargetingAnotherCreatureAreNotReduced() {
        harness.addToBattlefield(player1, new DwarvenMauler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip {3} targeting Dwarven Mauler costs exactly {1}")
    void equipThreeCostsOneMana() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new DwarvenMauler());
        Permanent warhammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, mauler.getId());
        harness.passBothPriorities();

        assertThat(warhammer.getAttachedTo()).isEqualTo(mauler.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Another Dwarven Mauler does not further reduce equip targeting the first")
    void reductionOnlyComesFromTheTargetedMauler() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new DwarvenMauler());
        harness.addToBattlefield(player1, new DwarvenMauler());
        harness.addToBattlefield(player1, new LoxodonWarhammer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, mauler.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reconfigure targeting Dwarven Mauler still costs its full mana cost")
    void reconfigureIsNotAnEquipAbility() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new DwarvenMauler());
        Permanent sash = harness.addToBattlefieldAndReturn(player1, new LionSash());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, mauler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sash.getAttachedTo()).isNull();
    }
}
