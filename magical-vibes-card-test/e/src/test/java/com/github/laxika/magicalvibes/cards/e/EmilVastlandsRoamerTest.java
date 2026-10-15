package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmilVastlandsRoamer.class, GrizzlyBears.class, Forest.class, Island.class, Mountain.class})
class EmilVastlandsRoamerTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have trample")
    void counteredOwnCreaturesHaveTrample() {
        harness.addToBattlefield(player1, new EmilVastlandsRoamer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creates a Fractal with one counter per differently named land")
    void createsFractalWithDistinctLandNameCounters() {
        addCreatureReady(player1, new EmilVastlandsRoamer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, fractal, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A Fractal with no land names dies as a 0/0")
    void createsZeroZeroFractalWithoutLands() {
        addCreatureReady(player1, new EmilVastlandsRoamer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Trample follows +1/+1 counters and applies only to your creatures, including Emil")
    void trampleRequiresOwnCreatureWithPlusOneCounter() {
        Permanent emil = harness.addToBattlefieldAndReturn(player1, new EmilVastlandsRoamer());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
        own.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isFalse();
        own.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        emil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, emil, Keyword.TRAMPLE)).isTrue();
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(emil);
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Ability counts only your current lands at resolution and survives Emil leaving")
    void countsCurrentControllerLandsAfterSourceLeaves() {
        Permanent emil = addCreatureReady(player1, new EmilVastlandsRoamer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(emil.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(emil);
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, fractal, Keyword.TRAMPLE)).isFalse();
        harness.assertNotOnBattlefield(player2, "Fractal");
    }
}
