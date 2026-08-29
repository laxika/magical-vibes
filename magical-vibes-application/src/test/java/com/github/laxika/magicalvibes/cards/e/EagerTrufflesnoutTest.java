package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EagerTrufflesnoutTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it deals combat damage to a player")
    void createsFoodTokenOnCombatDamageToPlayer() {
        addAttackingTrufflesnout(player1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Does not create a Food token when blocked")
    void doesNotCreateFoodTokenWhenBlocked() {
        addAttackingTrufflesnout(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    private Permanent addAttackingTrufflesnout(Player player) {
        Permanent attacker = addCreatureReady(player, new EagerTrufflesnout());
        attacker.setAttacking(true);
        return attacker;
    }
}
