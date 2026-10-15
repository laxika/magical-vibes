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

    @Test
    void ignoresLoreCountersOnOpposingSagasAndNonSagaPermanents() {
        Permanent tom = harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        addSaga(3);
        Permanent opposingSaga = harness.addToBattlefieldAndReturn(player2, new FableOfTheMirrorBreaker());
        opposingSaga.setCounterCount(CounterType.LORE, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.LORE, 4);

        assertThat(gqs.hasKeyword(gd, tom, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, tom, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void losesProtectionWhenLoreCounterTotalFallsBelowFour() {
        Permanent tom = harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        Permanent saga = addSaga(4);

        assertThat(gqs.hasKeyword(gd, tom, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, tom, Keyword.INDESTRUCTIBLE)).isTrue();

        saga.setCounterCount(CounterType.LORE, 3);

        assertThat(gqs.hasKeyword(gd, tom, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, tom, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void leavesAllCardsInLibraryWhenNoSagaIsRevealed() {
        harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        addSaga(2);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Reflection of Kiki-Jiki")).hasSize(1);
        assertThat(findPermanents(player1, "Fable of the Mirror-Breaker")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void doesNotTriggerForAnOpponentsFinalChapter() {
        harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        Permanent saga = harness.addToBattlefieldAndReturn(player2, new FableOfTheMirrorBreaker());
        saga.setCounterCount(CounterType.LORE, 2);
        FableOfTheMirrorBreaker librarySaga = new FableOfTheMirrorBreaker();
        harness.setLibrary(player1, List.of(librarySaga));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Reflection of Kiki-Jiki")).hasSize(1);
        assertThat(findPermanents(player1, "Fable of the Mirror-Breaker")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(librarySaga);
    }

    @Test
    void putsRevealedNonSagasBelowUnrevealedCards() {
        harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        addSaga(2);
        GrizzlyBears revealed = new GrizzlyBears();
        FableOfTheMirrorBreaker found = new FableOfTheMirrorBreaker();
        GrizzlyBears unrevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed, found, unrevealed));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fable of the Mirror-Breaker")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, revealed);
        assertThat(findPermanents(player1, "Fable of the Mirror-Breaker").getFirst()
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin Shaman")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenANonfinalChapterResolves() {
        harness.addToBattlefieldAndReturn(player1, new TomBombadil());
        addSaga(0);
        FableOfTheMirrorBreaker librarySaga = new FableOfTheMirrorBreaker();
        harness.setLibrary(player1, List.of(librarySaga));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Shaman")).hasSize(1);
        assertThat(findPermanents(player1, "Fable of the Mirror-Breaker")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(librarySaga);
    }
}
