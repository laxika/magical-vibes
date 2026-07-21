package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fertilid;
import com.github.laxika.magicalvibes.cards.w.WhiteManaBattery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolemnityTest extends BaseCardTest {

    @Test
    @DisplayName("A 0/0 that would enter with +1/+1 counters dies under Solemnity — the counters are never placed")
    void creatureEntersWithoutCounters() {
        harness.addToBattlefield(player1, new Solemnity());
        harness.setHand(player1, List.of(new Fertilid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Fertilid is printed 0/0 and normally enters with two +1/+1 counters. Solemnity prevents
        // those counters, so it enters as a 0/0 and dies to state-based actions.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fertilid"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fertilid"));
    }

    @Test
    @DisplayName("A charge counter can't be put on an artifact while Solemnity is on the battlefield")
    void chargeCounterNotPlaced() {
        Permanent battery = readyBattery(player1);
        harness.addToBattlefield(player1, new Solemnity());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // The cost is still paid (tapped) but the counter is never placed.
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without Solemnity the same activation places the charge counter")
    void chargeCounterPlacedWithoutSolemnity() {
        Permanent battery = readyBattery(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent readyBattery(Player player) {
        Permanent battery = harness.addToBattlefieldAndReturn(player, new WhiteManaBattery());
        battery.setSummoningSick(false);
        return battery;
    }
}
