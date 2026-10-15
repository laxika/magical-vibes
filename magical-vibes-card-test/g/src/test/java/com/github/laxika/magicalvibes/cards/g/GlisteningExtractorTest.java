package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlisteningExtractor.class, GrizzlyBears.class, LlanowarElves.class, Boomerang.class})
class GlisteningExtractorTest extends BaseCardTest {

    @Test
    void entersWithFourOilCounters() {
        harness.castFromHand(player1, new GlisteningExtractor(), "{2}{U}{B}");
        harness.passBothPriorities();

        Permanent extractor = findPermanent(player1, "Glistening Extractor");
        assertThat(extractor.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    void upkeepSeeksExactManaValueThenRemovesOneOilCounter() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 2);
        Card wrongManaValue = new LlanowarElves();
        Card matchingManaValue = new GrizzlyBears();
        harness.setLibrary(player1, List.of(wrongManaValue, matchingManaValue));

        harness.setHand(player1, List.of());
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(matchingManaValue);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(matchingManaValue);
        assertThat(extractor.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void upkeepDoesNotTriggerWithoutOilCounters() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 0);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of());
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(extractor.getCounterCount(CounterType.OIL)).isZero();
    }
    @Test
    void removesOilEvenWhenThereIsNoMatchingCard() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 3);
        Card nonmatching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatching));
        harness.setHand(player1, List.of());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
        assertThat(extractor.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    void usesCounterCountAtResolution() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 2);
        Card matching = new LlanowarElves();
        Card noLongerMatching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(noLongerMatching, matching));
        harness.setHand(player1, List.of());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            assertThat(gd.stack).hasSize(1);
            extractor.setCounterCount(CounterType.OIL, 1);
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(noLongerMatching);
        assertThat(extractor.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void doesNothingWhenLastOilCounterIsRemovedBeforeResolution() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 1);
        Card libraryCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            assertThat(gd.stack).hasSize(1);
            extractor.setCounterCount(CounterType.OIL, 0);
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void seeksUsingLastKnownCountersWhenSourceLeavesBeforeResolution() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 2);
        Card matching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(matching));
        harness.setHand(player1, List.of(new Boomerang()));

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            assertThat(gd.stack).hasSize(1);
            harness.addMana(player1, ManaColor.BLUE, 2);
            harness.castAndResolveInstant(player1, 0, extractor.getId());
            harness.assertNotOnBattlefield(player1, "Glistening Extractor");
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(matching);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
