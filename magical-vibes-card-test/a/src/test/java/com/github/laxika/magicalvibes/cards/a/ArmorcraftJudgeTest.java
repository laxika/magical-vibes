package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmorcraftJudge.class, GrizzlyBears.class, Forest.class})
class ArmorcraftJudgeTest extends BaseCardTest {

    @Test
    void entersAndDrawsForEachMatchingCreatureYouControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ArmorcraftJudge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 2);
    }

    @Test
    void ignoresCreaturesWithoutPlusOneCountersNoncreaturesAndOpponents() {
        Permanent matching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        matching.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        withoutCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Forest());
        noncreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ArmorcraftJudge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 1);
    }

    @Test
    void drawsOneCardPerCreatureRatherThanPerCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorcraftJudge());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new ArmorcraftJudge());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void drawsNothingWhenNoCreatureHasPlusOneCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorcraftJudge());
        creature.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new ArmorcraftJudge());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsCountersAddedBeforeResolutionIncludingOnTheJudgeItself() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorcraftJudge());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        Permanent judge = harness.enterBattlefieldAndReturn(player1, new ArmorcraftJudge());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        judge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotCountCountersRemovedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorcraftJudge());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new ArmorcraftJudge());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
