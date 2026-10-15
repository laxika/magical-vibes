package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaggerbackBasilisk.class, ColossalDreadmaw.class})
class DaggerbackBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Daggerback Basilisk destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new DaggerbackBasilisk());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        basilisk.setSummoningSick(false);
        basilisk.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(basilisk.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Daggerback Basilisk destroys a larger attacker when blocking")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent basilisk = harness.addToBattlefieldAndReturn(player2, new DaggerbackBasilisk());

        attacker.setAttacking(true);
        basilisk.setBlocking(true);
        basilisk.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(basilisk.getId(), 2, player2.getId(), 4));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(basilisk.getId()));
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Daggerback Basilisk");
    }

    @Test
    @DisplayName("Unblocked Daggerback Basilisk deals ordinary damage to a player")
    void deathtouchDoesNotDestroyPlayer() {
        Permanent basilisk = addCreatureReady(player1, new DaggerbackBasilisk());
        basilisk.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(basilisk.getId()));
    }
}
