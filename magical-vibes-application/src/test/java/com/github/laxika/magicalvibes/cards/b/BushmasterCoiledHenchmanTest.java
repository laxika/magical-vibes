package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BushmasterCoiledHenchman.class, GrizzlyBears.class})
class BushmasterCoiledHenchmanTest extends BaseCardTest {

    @Test
    void grantsDeathtouchToOtherCounteredCreaturesYouControl() {
        addBushmaster();
        Permanent counteredCreature = addCreature(player1, 1);
        Permanent uncounteredCreature = addCreature(player1, 0);
        Permanent opponentCreature = addCreature(player2, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void grantTracksPlusOnePlusOneCounters() {
        addBushmaster();
        Permanent creature = addCreature(player1, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addBushmaster() {
        return harness.addToBattlefieldAndReturn(player1, new BushmasterCoiledHenchman());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, int counterCount) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counterCount);
        return creature;
    }
}
