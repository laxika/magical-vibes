package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereGrid.class, GrizzlyBears.class})
class SphereGridTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with +1/+1 counters have reach and trample")
    void counteredCreaturesHaveReachAndTrample() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, countered, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A creature you control gets a +1/+1 counter after dealing combat damage")
    void creatureGetsCounterAfterCombatDamage() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(java.util.List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only your creatures get reach and trample")
    void opposingCreatureDoesNotGetKeywords() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, opposing, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.TRAMPLE)).isFalse();
    }
}
