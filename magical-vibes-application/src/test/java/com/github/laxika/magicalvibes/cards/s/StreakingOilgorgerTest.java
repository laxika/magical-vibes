package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StreakingOilgorgerTest extends BaseCardTest {

    @Test
    void gainsLifelinkAtMaxSpeedOnly() {
        Permanent oilgorger = addCreatureReady(player1, new StreakingOilgorger());
        gd.playerSpeeds.put(player1.getId(), 1);

        assertThat(gqs.hasKeyword(gd, oilgorger, Keyword.LIFELINK)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.hasKeyword(gd, oilgorger, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addCreatureReady(player1, new StreakingOilgorger());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }
}
