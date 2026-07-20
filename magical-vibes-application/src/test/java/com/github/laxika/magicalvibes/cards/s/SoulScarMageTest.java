package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoulScarMageTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to an opponent's creature is dealt as -1/-1 counters instead")
    void noncombatDamageToOpponentCreatureBecomesCounters() {
        harness.addToBattlefield(player1, new SoulScarMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(giant.getMarkedDamage()).isZero();
        // 3/3 with two -1/-1 counters survives as a 1/1.
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Noncombat damage to your own creature is dealt normally (opponent-only)")
    void noncombatDamageToOwnCreatureIsNormal() {
        harness.addToBattlefield(player1, new SoulScarMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        UUID targetId = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void prowessPumpsOnNoncreatureSpell() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SoulScarMage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve prowess trigger

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
    }
}
