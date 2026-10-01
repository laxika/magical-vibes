package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuvenileGloomwidow.class, FaerieMacabre.class, SafeholdSentry.class})
class JuvenileGloomwidowTest extends BaseCardTest {

    @Test
    @DisplayName("Deals combat damage to a blocker as -1/-1 counters, not marked damage")
    void witherDealsMinusCountersToBlocker() {
        addCreatureReady(player1, new JuvenileGloomwidow());
        Permanent blocker = addCreatureReady(player2, new SafeholdSentry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Wither does not poison a player — combat damage is normal life loss")
    void witherDoesNotPoisonPlayer() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new JuvenileGloomwidow());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent widow = addCreatureReady(player2, new JuvenileGloomwidow());
        addCreatureReady(player1, new FaerieMacabre());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(widow.isBlocking()).isTrue();
    }
}
