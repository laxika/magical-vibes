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

    private void triggerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
