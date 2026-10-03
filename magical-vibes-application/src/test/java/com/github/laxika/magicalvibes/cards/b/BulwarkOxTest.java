package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BulwarkOx.class})
class BulwarkOxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled puts a +1/+1 counter on target creature")
    void attacksWhileSaddledPutsCounterOnTargetCreature() {
        Permanent ox = addCreatureReady(player1, new BulwarkOx());
        ox.setSaddled(true);
        Permanent target = addCreatureReady(player2, new BulwarkOx());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not put a counter on a creature")
    void attacksWhileNotSaddledDoesNotPutCounter() {
        addCreatureReady(player1, new BulwarkOx());
        Permanent target = addCreatureReady(player2, new BulwarkOx());

        declareAttackers(player1, List.of(0));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing the Ox protects your creatures with counters until end of turn")
    void sacrificeGrantsKeywordsToCounteredCreaturesUntilEndOfTurn() {
        Permanent ox = addCreatureReady(player1, new BulwarkOx());
        Permanent countered = addCreatureReady(player1, new BulwarkOx());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncountered = addCreatureReady(player1, new BulwarkOx());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ox);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ox.getCard());
        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Saddle 1 taps another creature, including one with summoning sickness")
    void saddleEnablesAttackTrigger() {
        Permanent ox = addCreatureReady(player1, new BulwarkOx());
        Permanent saddler = harness.addToBattlefieldAndReturn(player1, new BulwarkOx());
        saddler.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(ox.isTapped()).isFalse();
        assertThat(ox.isSaddled()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ox.getId());
        resolveAllTriggers();

        assertThat(ox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The saddled attack trigger resolves after its source is sacrificed")
    void attackTriggerSurvivesSourceSacrifice() {
        Permanent ox = addCreatureReady(player1, new BulwarkOx());
        ox.setSaddled(true);
        Permanent target = addCreatureReady(player1, new BulwarkOx());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ox);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protection accepts any counter and excludes opposing creatures")
    void protectionIncludesNonPlusOneCountersButNotOpponents() {
        addCreatureReady(player1, new BulwarkOx());
        Permanent own = addCreatureReady(player1, new BulwarkOx());
        own.setCounterCount(CounterType.STUN, 1);
        Permanent opponent = addCreatureReady(player2, new BulwarkOx());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, own, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protection determines recipients at resolution and keeps that group fixed")
    void protectionSnapshotsCreaturesAtResolution() {
        addCreatureReady(player1, new BulwarkOx());
        Permanent gainsCounter = addCreatureReady(player1, new BulwarkOx());
        Permanent losesCounter = addCreatureReady(player1, new BulwarkOx());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.INDESTRUCTIBLE)).isFalse();

        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent newcomer = addCreatureReady(player1, new BulwarkOx());
        newcomer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
