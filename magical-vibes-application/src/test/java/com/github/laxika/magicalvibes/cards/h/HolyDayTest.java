package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HolyDay.class, GrizzlyBears.class})
class HolyDayTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.castFromHand(player1, new HolyDay(), "{W}");

        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage while Holy Day is in effect")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new HolyDay(), "{W}");

        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage to attacking and blocking creatures")
    void preventsCombatDamageToCreatures() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new HolyDay(), "{W}");
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }
}
