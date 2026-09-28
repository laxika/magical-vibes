package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialOfATimeLord.class, GrizzlyBears.class})
class TrialOfATimeLordTest extends BaseCardTest {

    @Test
    void chapterTargetsOnlyNontokenCreaturesAnOpponentControls() {
        addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card firstCard = first.getCard();
        Card secondCard = second.getCard();
        harness.setLibrary(player2, new ArrayList<>());

        resolveExileChapter(saga, first);
        resolveExileChapter(saga, second);
        resolveExileChapter(saga, null);

        Player player3 = addThirdPlayer();
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);
        harness.handleListChoice(player3, ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT);

        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == firstCard || exiled.card() == secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void innocentMajorityReturnsExiledCardsWhenSagaLeaves() {
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveExileChapter(saga, target);
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT);
        harness.handleListChoice(player2, ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(target.getCard());
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TrialOfATimeLord());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void resolveExileChapter(Permanent saga, Permanent target) {
        advanceToNextChapter();
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
