package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault11VotersDilemma.class, GrizzlyBears.class, Forest.class})
class Vault11VotersDilemmaTest extends BaseCardTest {

    @Test
    void chapterICreatesOneHumanSoldierForEachOpponent() {
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
    }

    @Test
    void chapterIIDestroysCreaturesTiedForMostVotes() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        assertThat(activeVote().validIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second);
    }

    @Test
    void chapterIIINoVotesMakesEachPlayerDraw() {
        Forest player1Draw = new Forest();
        Forest player2Draw = new Forest();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
        addCreatureReady(player1, new GrizzlyBears());
        addSagaWithLore(2);

        advanceToNextChapter();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Draw);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault11VotersDilemma());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private PendingInteraction.MultiPermanentChoice activeVote() {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        return choice;
    }
}
