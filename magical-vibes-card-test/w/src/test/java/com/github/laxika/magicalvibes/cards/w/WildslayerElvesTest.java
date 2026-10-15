package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildslayerElves.class, CrabappleCohort.class})
class WildslayerElvesTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Wither deals combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusCountersToBlocker() {
        // 5/5 blocker so it survives and we can inspect the counters.
        Permanent blocker = addCreatureReady(player2, createCreature("Big Bear", 5, 5));

        Permanent attacker = addCreatureReady(player1, new WildslayerElves());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Wildslayer Elves (3 power) deals its damage as three -1/-1 counters; 5/5 becomes 2/2.
        Permanent survivor = findPermanent(player2, "Big Bear");
        assertThat(survivor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wither combat damage of lethal counters kills the blocker")
    void witherKillsSmallBlocker() {
        Permanent attacker = addCreatureReady(player1, new WildslayerElves());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, createCreature("Small Bear", 3, 3));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Three -1/-1 counters make the 3/3 a 0/0; it dies.
        harness.assertInGraveyard(player2, "Small Bear");
    }

    @Test
    @DisplayName("Wither deals normal life loss to a player, not -1/-1 counters")
    void witherDealsNormalDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new WildslayerElves());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Wither also puts counters on an attacker when Wildslayer Elves blocks")
    void witherDealsMinusCountersWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new CrabappleCohort());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WildslayerElves());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crabapple Cohort");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Wildslayer Elves");
        harness.assertLife(player2, 20);
    }
}
