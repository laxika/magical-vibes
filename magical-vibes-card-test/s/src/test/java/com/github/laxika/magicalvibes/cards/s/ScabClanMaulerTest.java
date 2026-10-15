package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ScabClanMauler.class)
class ScabClanMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 2: enters with two +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castMauler();

        assertThat(findPermanent(player1, "Scab-Clan Mauler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodthirst 2: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castMauler();

        assertThat(findPermanent(player1, "Scab-Clan Mauler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 2 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castMauler();

        assertThat(findPermanent(player1, "Scab-Clan Mauler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst adds exactly two counters regardless of the amount of damage")
    void bloodthirstCounterCountIsFixed() {
        gd.recordDamageToPlayer(player2.getId(), 5);
        gd.recordDamageToPlayer(player2.getId(), 3);
        castMauler();

        assertThat(findPermanent(player1, "Scab-Clan Mauler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Life loss without damage does not enable bloodthirst")
    void bloodthirstIgnoresLifeLoss() {
        harness.setLife(player2, 15);
        castMauler();

        assertThat(findPermanent(player1, "Scab-Clan Mauler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst checks damage when the creature enters, rather than when it is cast")
    void bloodthirstChecksDamageOnResolution() {
        harness.castFromHand(player1, new ScabClanMauler(), "{R}{G}");
        assertThat(findPermanents(player1, "Scab-Clan Mauler")).isEmpty();

        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Scab-Clan Mauler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample: assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ScabClanMauler());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new ScabClanMauler());

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ));

        assertThat(findPermanents(player2, "Scab-Clan Mauler")).isEmpty();
        harness.assertLife(player2, 18);
    }

    private void castMauler() {
        harness.castFromHand(player1, new ScabClanMauler(), "{R}{G}");
        resolveAllTriggers();
    }
}
