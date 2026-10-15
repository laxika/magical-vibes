package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightOfTheFlyingMerfolk.class, Forest.class, GrizzlyBears.class, StormCrow.class})
class NightOfTheFlyingMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Bedtime story waits until the end step before adding lore")
    void waitsUntilEndStep() {
        harness.castFromHand(player1, new NightOfTheFlyingMerfolk(), "{2}{U}");
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "Night of the Flying Merfolk");
        assertThat(saga.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.stack).isEmpty();

        triggerEndStep();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter II gives flying counters only to tapped creatures")
    void chapterIIGivesFlyingCountersToTappedCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightOfTheFlyingMerfolk());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
        Permanent untapped = addCreatureReady(player1, new GrizzlyBears());
        tapped.tap();

        triggerEndStep();

        assertThat(tapped.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(untapped.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    @DisplayName("Chapter III draws for creatures that dealt combat damage this turn")
    void chapterIIIDrawsForCombatDamageCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightOfTheFlyingMerfolk());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new StormCrow());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(1));
        resolveCombat();
        triggerEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Night of the Flying Merfolk"));
    }

    @Test
    @DisplayName("Chapter II excludes opposing creatures and tapped noncreatures")
    void chapterIIExcludesOpposingCreaturesAndNoncreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightOfTheFlyingMerfolk());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownCreature.tap();
        opposingCreature.tap();
        land.tap();

        triggerEndStep();

        assertThat(ownCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(land.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    @DisplayName("Chapter III draws nothing without qualifying combat damage and still sacrifices the Saga")
    void chapterIIIDrawsNothingWithoutCombatDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightOfTheFlyingMerfolk());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        triggerEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Night of the Flying Merfolk");
    }

    @Test
    @DisplayName("Chapter III counts creatures rather than damage and excludes creatures that did not attack")
    void chapterIIICountsEachQualifyingCreatureOnce() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightOfTheFlyingMerfolk());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(1, 2));
        resolveCombat();
        triggerEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The opponent's end step does not advance bedtime story")
    void opponentsEndStepDoesNotAddLore() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightOfTheFlyingMerfolk());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(saga.getCounterCount(CounterType.LORE)).isZero();
        assertThat(countPermanents(player1, "Merfolk")).isZero();
    }

    private void triggerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
