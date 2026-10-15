package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AragornHornburgHero.class, GrizzlyBears.class})
class AragornHornburgHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures gain first strike and renown 1")
    void attackingCreaturesGainFirstStrikeAndRenown() {
        addCreatureReady(player1, new AragornHornburgHero());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();

        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Combat damage from a renowned creature doubles its +1/+1 counters")
    void renownedCreatureDoublesCounters() {
        addCreatureReady(player1, new AragornHornburgHero());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bear.setRenowned(true);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
