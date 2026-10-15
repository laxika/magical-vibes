package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialOfATimeLord.class, ClockworkDroid.class, AdiposeOffspring.class})
class TrialOfATimeLordTest extends BaseCardTest {

    @Test
    void chapterTargetsOnlyNontokenCreaturesAnOpponentControls() {
        addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2, tokenCreature());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId(), opponentToken.getId());
    }

    @Test
    void guiltyMajorityPutsCardsExiledWithSagaOnOwnersLibraries() {
        Permanent saga = addSagaWithLore(0);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        Card firstCard = first.getCard();
        Card secondCard = second.getCard();
        harness.setLibrary(player2, new ArrayList<>());

        resolveExileChapter(first);
        resolveExileChapter(second);
        resolveExileChapter(null);

        Player player3 = addThirdPlayer();
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);
        harness.handleListChoice(player3, ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(secondCard), reorder.cards().indexOf(firstCard))));

        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == firstCard || exiled.card() == secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard, firstCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void tiedVoteReturnsExiledCardsWhenSagaLeaves() {
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());

        resolveExileChapter(target);
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT);
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void enteringSagaExilesTheFirstChapterTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        harness.castFromHand(player1, new TrialOfATimeLord(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    void chapterCannotTargetAnOpponentsNoncreaturePermanent() {
        addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new TrialOfATimeLord());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(enchantment.getId());
    }

    @Test
    void thirdChapterExilesAnotherCreature() {
        addSagaWithLore(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());

        resolveExileChapter(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    void leavingBeforeExileChapterResolvesDoesNotExileTarget() {
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, saga));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    void leavingBeforeFinalChapterReturnsExiledCreature() {
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        resolveExileChapter(target);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, saga));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == target.getCard());
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    void innocentMajorityReturnsCreatureAfterFinalChapter() {
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        resolveExileChapter(target);
        Player player3 = addThirdPlayer();
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT);
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
        harness.handleListChoice(player3, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void guiltyVoteMovesOnlyCardsExiledWithThatSaga() {
        Permanent otherSaga = addSagaWithLore(0);
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        resolveExileChapter(otherTarget);
        Permanent saga = harness.addToBattlefieldAndReturn(player2, new TrialOfATimeLord());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of());
        saga.setCounterCount(CounterType.LORE, 3);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice firstVote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstVote.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);
        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == otherTarget.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherSaga);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(saga);
    }

    @Test
    void guiltyVotePutsStolenCreatureInItsOwnersLibrary() {
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        resolveExileChapter(target);
        harness.setLibrary(player1, List.of());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() == target.getCard());
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TrialOfATimeLord());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void resolveExileChapter(Permanent target) {
        advanceToNextChapter();
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private Card tokenCreature() {
        Card token = new Card();
        token.setToken(true);
        token.setType(CardType.CREATURE);
        token.setName("Token Creature");
        token.setPower(1);
        token.setToughness(1);
        return token;
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerIdToName.put(player3Id, player3.getUsername());
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new com.github.laxika.magicalvibes.model.ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
