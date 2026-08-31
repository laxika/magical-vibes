package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlightedBat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonstoneHarbinger.class, BlightedBat.class, GrizzlyBears.class})
class MoonstoneHarbingerTest extends BaseCardTest {

    @Test
    void lifeGainBoostsOwnBatsAndGrantsThemDeathtouch() {
        Permanent harbinger = addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBat = addCreatureReady(player2, new BlightedBat());
        int batPower = bat.getEffectivePower();

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeGainTriggers(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(harbinger.getEffectivePower()).isEqualTo(2);
        assertThat(bat.getEffectivePower()).isEqualTo(batPower + 1);
        assertThat(gqs.hasKeyword(gd, bat, Keyword.DEATHTOUCH)).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
        assertThat(opposingBat.getEffectivePower()).isEqualTo(batPower);
        assertThat(gqs.hasKeyword(gd, opposingBat, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void lifeLossAlsoTriggersDuringYourTurn() {
        addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        int batPower = bat.getEffectivePower();

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(bat.getEffectivePower()).isEqualTo(batPower + 1);
        assertThat(gqs.hasKeyword(gd, bat, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        int batPower = bat.getEffectivePower();

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeGainTriggers(gd, player1.getId(), 1));
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(bat.getEffectivePower()).isEqualTo(batPower + 1);
    }

    @Test
    void doesNotTriggerDuringOpponentTurn() {
        addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        int batPower = bat.getEffectivePower();

        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeGainTriggers(gd, player1.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1);
        });

        assertThat(gd.stack).isEmpty();
        assertThat(bat.getEffectivePower()).isEqualTo(batPower);
        assertThat(gqs.hasKeyword(gd, bat, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void boostAndDeathtouchWearOffAtEndOfTurn() {
        addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        int batPower = bat.getEffectivePower();

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeGainTriggers(gd, player1.getId(), 1));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bat.getEffectivePower()).isEqualTo(batPower);
        assertThat(gqs.hasKeyword(gd, bat, Keyword.DEATHTOUCH)).isFalse();
    }
}
