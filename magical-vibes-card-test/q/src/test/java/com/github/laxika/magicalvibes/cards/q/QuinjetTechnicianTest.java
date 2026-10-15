package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.v.VivVisionTeenSynthezoid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuinjetTechnician.class, AnabaShaman.class, VivVisionTeenSynthezoid.class})
class QuinjetTechnicianTest extends BaseCardTest {

    @Test
    void firstAbilityAddsUnrestrictedRedMana() {
        addReadyTechnician();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void secondAbilityAddsPowerUpOnlyRedMana() {
        addReadyTechnician();

        harness.activateAbility(player1, 0, 1, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getPowerUpAbilityOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    void secondAbilityManaCannotPayAnOrdinaryActivatedAbility() {
        addReadyTechnician();
        addCreatureReady(player1, new AnabaShaman());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void secondAbilityManaPaysPowerUpAbility() {
        addReadyTechnician();
        addReadyTechnician();
        Permanent viv = harness.enterBattlefieldAndReturn(player1, new VivVisionTeenSynthezoid());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(viv.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void secondAbilityManaCannotCastCreatureSpell() {
        addReadyTechnician();
        harness.setHand(player1, List.of(new QuinjetTechnician()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerUpAbilityOnlyMana(ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    void firstAbilityTapsSourceAndPreventsSecondActivation() {
        Permanent technician = addReadyTechnician();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(technician.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerUpAbilityOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    void secondAbilityTapsSourceAndPreventsFirstActivation() {
        Permanent technician = addReadyTechnician();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(technician.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerUpAbilityOnlyMana(ManaColor.RED))
                .isEqualTo(2);
    }

    private Permanent addReadyTechnician() {
        return addCreatureReady(player1, new QuinjetTechnician());
    }
}
