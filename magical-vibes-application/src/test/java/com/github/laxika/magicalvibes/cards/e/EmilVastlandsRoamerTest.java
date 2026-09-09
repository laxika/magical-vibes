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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent emil = harness.addToBattlefieldAndReturn(player1, new EmilVastlandsRoamer());
        emil.setSummoningSick(false);
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
        Permanent emil = harness.addToBattlefieldAndReturn(player1, new EmilVastlandsRoamer());
        emil.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()));
    }
}
