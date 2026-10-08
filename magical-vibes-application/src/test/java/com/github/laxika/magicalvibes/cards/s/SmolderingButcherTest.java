package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BallynockTrapper;
import com.github.laxika.magicalvibes.cards.d.Doomgape;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingButcher.class, BallynockTrapper.class, Doomgape.class, SoulsFire.class})
class SmolderingButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Wither: combat damage to a blocker is dealt as -1/-1 counters, not marked damage")
    void witherDealsMinusCountersToBlocker() {
        Permanent butcher = addCreatureReady(player1, new SmolderingButcher()); // 4/2, wither
        butcher.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BallynockTrapper()); // 2/2
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // All 4 combat damage is dealt to the sole blocker as -1/-1 counters rather than marked damage.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Wither does not poison players — combat damage to a player is normal life loss")
    void witherDoesNotPoisonPlayer() {
        harness.setLife(player2, 20);

        Permanent butcher = addCreatureReady(player1, new SmolderingButcher()); // 4/2, wither
        butcher.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    void witherAlsoAppliesWhenButcherBlocks() {
        Permanent attacker = addCreatureReady(player1, new BallynockTrapper());
        attacker.setAttacking(true);
        Permanent butcher = addCreatureReady(player2, new SmolderingButcher());
        butcher.setBlocking(true);
        butcher.addBlockingTarget(0);

        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(butcher);
    }

    @Test
    void survivingBlockerHasReducedPowerAndToughness() {
        Permanent butcher = addCreatureReady(player1, new SmolderingButcher());
        butcher.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Doomgape());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(butcher);
    }

    @Test
    void witherAppliesToNoncombatDamageDealtByButcher() {
        Permanent butcher = addCreatureReady(player1, new SmolderingButcher());
        Permanent target = addCreatureReady(player2, new Doomgape());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(butcher.getId(), target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }
}
