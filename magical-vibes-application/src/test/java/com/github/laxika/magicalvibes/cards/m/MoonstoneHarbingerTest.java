package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonebindOrator;
import com.github.laxika.magicalvibes.cards.g.GlidediveDuo;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonstoneHarbinger.class, GlidediveDuo.class, BonebindOrator.class})
class MoonstoneHarbingerTest extends BaseCardTest {

    @Test
    void lifeGainBoostsOwnBatsAndGrantsThemDeathtouch() {
        Permanent harbinger = addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent bat = addCreatureReady(player1, new GlidediveDuo());
        Permanent bear = addCreatureReady(player1, new BonebindOrator());
        Permanent opposingBat = addCreatureReady(player2, new GlidediveDuo());
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
        Permanent bat = addCreatureReady(player1, new GlidediveDuo());
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
        Permanent bat = addCreatureReady(player1, new GlidediveDuo());
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
        Permanent bat = addCreatureReady(player1, new GlidediveDuo());
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
        Permanent bat = addCreatureReady(player1, new GlidediveDuo());
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
    @Test
    void payingLifeTriggersAndPreventsAnotherTriggerFromLifeGain() {
        Permanent harbinger = addCreatureReady(player1, new MoonstoneHarbinger());
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifePayment(gd, player1.getId(), 2, "Life payment"));
        resolveAllTriggers();
        harness.assertLife(player1, 18);
        assertThat(harbinger.getEffectivePower()).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        assertThat(gd.stack).isEmpty();
        assertThat(harbinger.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void eachHarbingerHasItsOwnOncePerTurnLimit() {
        Permanent first = addCreatureReady(player1, new MoonstoneHarbinger());
        Permanent second = addCreatureReady(player1, new MoonstoneHarbinger());
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() ->
                harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "Life loss"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void affectsBatsPresentAtResolutionButNotBatsEnteringAfterward() {
        addCreatureReady(player1, new MoonstoneHarbinger());
        harness.forceActivePlayer(player1);
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));

        Permanent beforeResolution = addCreatureReady(player1, new GlidediveDuo());
        int basePower = beforeResolution.getEffectivePower();
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new GlidediveDuo());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(basePower + 1);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.DEATHTOUCH)).isTrue();
        assertThat(afterResolution.getEffectivePower()).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void opponentsLifeChangesDoNotUseControllersTriggerLimit() {
        Permanent harbinger = addCreatureReady(player1, new MoonstoneHarbinger());
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 2);
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "Life loss");
        });
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() ->
                harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        resolveAllTriggers();
        assertThat(harbinger.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void furtherLifeChangesBeforeResolutionDoNotCreateAdditionalTriggers() {
        Permanent harbinger = addCreatureReady(player1, new MoonstoneHarbinger());
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "Life loss");
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
        });
        resolveAllTriggers();

        assertThat(harbinger.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void zeroLifeChangesDoNotConsumeTheTriggerLimit() {
        Permanent harbinger = addCreatureReady(player1, new MoonstoneHarbinger());
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 0, "Life loss");
        });
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "Life loss"));
        resolveAllTriggers();
        assertThat(harbinger.getEffectivePower()).isEqualTo(2);
    }
}
