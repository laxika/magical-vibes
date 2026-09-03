package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaMine.class, CloudElemental.class})
class MagmaMineTest extends BaseCardTest {

    @Test
    @DisplayName("{4} puts a pressure counter on Magma Mine")
    void addPressureCounter() {
        Permanent mine = addReadyMine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mine.getCounterCount(CounterType.PRESSURE)).isEqualTo(1);
        assertThat(mine.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple {4} activations accumulate pressure counters")
    void multipleActivationsAccumulateCounters() {
        Permanent mine = addReadyMine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mine.getCounterCount(CounterType.PRESSURE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice deals damage equal to pressure counters to target player")
    void sacrificeDealsDamageToPlayer() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.PRESSURE, 5);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player1, "Magma Mine");
        harness.assertInGraveyard(player1, "Magma Mine");
    }

    @Test
    @DisplayName("Sacrifice with 0 counters deals 0 damage")
    void sacrificeWithZeroCountersDealsZeroDamage() {
        addReadyMine(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Magma Mine");
    }

    @Test
    @DisplayName("Sacrifice deals damage to target creature")
    void sacrificeDealsDamageToCreature() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.PRESSURE, 3);

        harness.addToBattlefield(player2, new CloudElemental());
        UUID elementalId = harness.getPermanentId(player2, "Cloud Elemental");

        harness.activateAbility(player1, 0, 1, null, elementalId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloud Elemental");
        harness.assertInGraveyard(player2, "Cloud Elemental");
        harness.assertNotOnBattlefield(player1, "Magma Mine");
    }

    @Test
    @DisplayName("Pressure counters are snapshotted before sacrifice so damage is correct")
    void pressureCountersSnapshotBeforeSacrifice() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.PRESSURE, 7);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Magma Mine");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Sacrifice ability requires tap — cannot activate when tapped")
    void sacrificeAbilityRequiresTap() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.PRESSURE, 3);
        mine.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMine(Player player) {
        MagmaMine card = new MagmaMine();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
