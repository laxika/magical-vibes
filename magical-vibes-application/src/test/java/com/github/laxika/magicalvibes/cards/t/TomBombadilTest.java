package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FableOfTheMirrorBreaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReflectionOfKikiJiki;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TomBombadil.class, FableOfTheMirrorBreaker.class, ReflectionOfKikiJiki.class, GrizzlyBears.class})
class TomBombadilTest extends BaseCardTest {

    @Test
    void gainsHexproofAndIndestructibleFromFourLoreCountersAmongSagas() {
        Permanent tom = harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        Permanent firstSaga = addSaga(2);
        Permanent secondSaga = addSaga(1);

        assertThat(gqs.hasKeyword(gd, tom, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, tom, Keyword.INDESTRUCTIBLE)).isFalse();

        secondSaga.setCounterCount(CounterType.LORE, 2);

        assertThat(gqs.hasKeyword(gd, tom, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, tom, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(firstSaga.getCounterCount(CounterType.LORE)
                + secondSaga.getCounterCount(CounterType.LORE)).isEqualTo(4);
    }

    @Test
    void putsASagaOntoTheBattlefieldAfterOnlyOneFinalChapterResolvesEachTurn() {
        harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        addSaga(2);
        addSaga(2);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new FableOfTheMirrorBreaker(),
                new GrizzlyBears(), new FableOfTheMirrorBreaker()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Reflection of Kiki-Jiki")).hasSize(2);
        assertThat(findPermanents(player1, "Fable of the Mirror-Breaker")).hasSize(1);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FableOfTheMirrorBreaker());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }
}
