package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ModelOfUnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault11VotersDilemma.class, GrizzlyBears.class, Forest.class, ModelOfUnity.class})
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

    @Test
    void enteringBattlefieldTriggersFirstChapter() {
        harness.enterBattlefieldAndReturn(player1, new Vault11VotersDilemma());

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Human Soldier")).isZero();
    }

    @Test
    void unanimousVotesDestroyOnlyTheVotedCreatureAfterBothPlayersVote() {
        Permanent selected = addCreatureReady(player1, new GrizzlyBears());
        Permanent unselected = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(selected);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unselected);

        harness.handleMultiplePermanentsChosen(player2, List.of(selected.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(selected);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unselected);
    }

    @Test
    void singleVoteStillDestroysCreatureWhenOtherPlayerAbstains() {
        Permanent selected = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player2, List.of(selected.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(selected);
    }

    @Test
    void emptyBattlefieldMakesEachPlayerDrawWithoutRequestingVotes() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw));
        harness.setLibrary(player2, List.of(secondDraw));
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw);
        assertThat(gd.playerHands.get(player2.getId())).contains(secondDraw);
    }

    @Test
    void finishingVotesTriggersModelOfUnityForBothMatchingVoters() {
        harness.enterBattlefieldAndReturn(player1, new ModelOfUnity());
        harness.enterBattlefieldAndReturn(player1, new Vault11VotersDilemma());
        resolveAllTriggers();
        Permanent soldier = findPermanent(player1, "Human Soldier");

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(soldier.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(soldier.getId()));
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.MayAbilityChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
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
