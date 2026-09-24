package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AgentMariaHill;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheHulkWallbreaker.class, AgentMariaHill.class, GrizzlyBears.class, LukeCagePowerMan.class})
class SheHulkWallbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Heroes you control have trample")
    void grantsTrampleToOtherHeroesYouControl() {
        addCreatureReady(player1, new SheHulkWallbreaker());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent ownNonHero = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingHero = addCreatureReady(player2, new LukeCagePowerMan());

        assertThat(gqs.hasKeyword(gd, ownHero, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonHero, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingHero, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A Hero becoming blocked gets one +1/+1 counter per blocker")
    void putsCountersEqualToBlockersOnBecomingBlocked() {
        Permanent sheHulk = addCreatureReady(player1, new SheHulkWallbreaker());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());
        hero.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
