package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainedThroatseeker.class, ChancellorOfTheTangle.class})
class ChainedThroatseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when defending player is poisoned")
    void canAttackWhenDefenderPoisoned() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ChainedThroatseeker());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackers(player1, List.of(0));

        // Attack went through — defender gets poison counters from infect (not life loss)
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isGreaterThan(1);
    }

    @Test
    @DisplayName("Cannot attack when defending player has no poison counters")
    void cannotAttackWhenDefenderNotPoisoned() {
        addCreatureReady(player1, new ChainedThroatseeker());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the controller is poisoned")
    void cannotAttackWhenOnlyControllerPoisoned() {
        addCreatureReady(player1, new ChainedThroatseeker());
        gd.playerPoisonCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when defender has multiple poison counters")
    void canAttackWhenDefenderHasMultiplePoisonCounters() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ChainedThroatseeker());
        gd.playerPoisonCounters.put(player2.getId(), 7);

        declareAttackers(player1, List.of(0));

        // Attack went through — poison counters increased by 5 (power)
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Deals damage to players as poison counters (infect)")
    void dealsPoisonCountersToPlayers() {
        harness.setLife(player2, 20);
        Permanent perm = addCreatureReady(player1, new ChainedThroatseeker());
        perm.setAttacking(true);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveCombat();

        // Infect deals poison counters instead of life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(6); // 1 existing + 5 from combat
    }

    @Test
    @DisplayName("Can block an unpoisoned player and deals infect damage to their creature")
    void canBlockWithoutPoisonAndDealsCounters() {
        Permanent attacker = addCreatureReady(player1, new ChancellorOfTheTangle());
        addCreatureReady(player2, new ChainedThroatseeker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Chained Throatseeker");
        harness.assertNotOnBattlefield(player2, "Chained Throatseeker");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Remains attacking if the defender loses their last poison counter")
    void remainsAttackingAfterPoisonIsRemoved() {
        addCreatureReady(player1, new ChainedThroatseeker());
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gd.playerPoisonCounters.remove(player2.getId());
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
        harness.assertLife(player2, 20);
    }
}
