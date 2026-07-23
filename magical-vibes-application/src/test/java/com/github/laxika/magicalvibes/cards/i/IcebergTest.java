package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcebergTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 ice counters")
    void entersWithXIceCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Iceberg()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent iceberg = findIceberg(player1);
        assertThat(iceberg.getCounterCount(CounterType.ICE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters with 0 ice counters")
    void entersWithZeroIceCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Iceberg()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent iceberg = findIceberg(player1);
        assertThat(iceberg.getCounterCount(CounterType.ICE)).isEqualTo(0);
    }

    @Test
    @DisplayName("{3} puts an ice counter on this enchantment")
    void payThreeAddsIceCounter() {
        Permanent iceberg = addReadyIceberg(player1, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(iceberg.getCounterCount(CounterType.ICE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing an ice counter adds {C}")
    void removeCounterAddsColorlessMana() {
        Permanent iceberg = addReadyIceberg(player1, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(before + 1);
        assertThat(iceberg.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot remove an ice counter when none remain")
    void cannotActivateWithoutIceCounters() {
        Permanent iceberg = addReadyIceberg(player1, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyIceberg(Player player, int iceCounters) {
        Permanent perm = new Permanent(new Iceberg());
        perm.setCounterCount(CounterType.ICE, iceCounters);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findIceberg(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Iceberg"))
                .findFirst().orElseThrow();
    }
}
