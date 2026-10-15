package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacleOfDestruction.class, GrizzlyBears.class, WrathOfGod.class, Forest.class, LurkerInTheDeep.class})
class SpectacleOfDestructionTest extends BaseCardTest {

    @Test
    void simultaneousCreatureDeathsPutOneWreckCounterOnSpectacle() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isEqualTo(1);
    }

    @Test
    void upkeepRemovesWreckCounterAndSeeksNonlandCard() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void opponentCreatureDeathPutsWreckCounterOnSpectacle() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isEqualTo(1);
    }

    @Test
    void separateDeathsInSameTurnEachPutWreckCounterOnSpectacle() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        first.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        second.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isEqualTo(2);
    }

    @Test
    void upkeepWithoutWreckCounterDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpectacleOfDestruction());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentUpkeepDoesNotRemoveCounterOrSeek() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isEqualTo(1);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void upkeepRemovesOnlyOneCounterWhenSeveralArePresent() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WrathOfGod()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void upkeepRechecksWreckCountersBeforeResolving() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        spectacle.setCounterCount(CounterType.WRECK, 0);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void upkeepStillRemovesCounterWhenLibraryHasOnlyLands() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void upkeepWithEmptyLibraryRemovesCounterWithoutDrawingOrLosing() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @CardUsed({SpectacleOfDestruction.class, LurkerInTheDeep.class})
    void upkeepSeekTriggersLurkerAndManifestsDuplicate() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.addToBattlefield(player1, new LurkerInTheDeep());
        SpectacleOfDestruction sought = new SpectacleOfDestruction();
        harness.setLibrary(player1, List.of(sought));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested).hasSize(1)
                .allSatisfy(permanent -> {
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.getCard().getId()).isNotEqualTo(sought.getId());
                });
    }

    @Test
    void seekingDoesNotDiscloseSelectedCardInPublicLog() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int logSize = gd.gameLog.size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()))
                .noneMatch(entry -> entry.plainText().contains("Grizzly Bears"));
    }
}
