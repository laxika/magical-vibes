package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StreakingOilgorger.class})
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

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void combatDamageAtMaxSpeedGainsLife() {
        Permanent oilgorger = addCreatureReady(player1, new StreakingOilgorger());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        oilgorger.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void damageThatTriggersMaxSpeedDoesNotRetroactivelyGainLife() {
        Permanent oilgorger = addCreatureReady(player1, new StreakingOilgorger());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        oilgorger.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);

        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, oilgorger, Keyword.LIFELINK)).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsMaxSpeedDoesNotGrantLifelink() {
        Permanent oilgorger = addCreatureReady(player1, new StreakingOilgorger());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThat(gqs.hasKeyword(gd, oilgorger, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentLosingLifeOnTheirTurnDoesNotIncreaseControllerSpeed() {
        addCreatureReady(player1, new StreakingOilgorger());
        gd.playerSpeeds.put(player1.getId(), 1);
        harness.forceActivePlayer(player2);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void controllersOwnLifeLossDoesNotIncreaseSpeed() {
        addCreatureReady(player1, new StreakingOilgorger());
        gd.playerSpeeds.put(player1.getId(), 1);
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void anotherOilgorgerDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        addCreatureReady(player1, new StreakingOilgorger());

        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }
}
