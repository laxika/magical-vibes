package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({GlisteningExtractor.class, GrizzlyBears.class, LlanowarElves.class})
class GlisteningExtractorTest extends BaseCardTest {

    @Test
    void entersWithFourOilCounters() {
        harness.setHand(player1, List.of(new GlisteningExtractor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
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

        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(matchingManaValue);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(matchingManaValue);
        assertThat(extractor.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void upkeepDoesNotTriggerWithoutOilCounters() {
        Permanent extractor = harness.addToBattlefieldAndReturn(player1, new GlisteningExtractor());
        extractor.setCounterCount(CounterType.OIL, 0);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(extractor.getCounterCount(CounterType.OIL)).isZero();
    }
}
