package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeerDog.class, HonorGuard.class})
class DeerDogTest extends BaseCardTest {

    @Test
    @DisplayName("First strike deals combat damage before a 1/1 blocker")
    void firstStrikeDealsDamageFirst() {
        Permanent attacker = addCreatureReady(player1, new DeerDog());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new HonorGuard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Deer-Dog");
        harness.assertInGraveyard(player2, "Honor Guard");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("First strike kills an attacking 1/1 before it damages Deer-Dog")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new HonorGuard());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new DeerDog());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertOnBattlefield(player2, "Deer-Dog");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Deer-Dog deals damage only once")
    void unblockedFirstStrikerDoesNotDealRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new DeerDog());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Deer-Dog");
    }

    @Test
    @DisplayName("Two Deer-Dogs each deal first-strike damage only once")
    void bothFirstStrikersDealDamage() {
        Permanent attacker = addCreatureReady(player1, new DeerDog());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new DeerDog());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Deer-Dog");
        harness.assertOnBattlefield(player2, "Deer-Dog");
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }
}
