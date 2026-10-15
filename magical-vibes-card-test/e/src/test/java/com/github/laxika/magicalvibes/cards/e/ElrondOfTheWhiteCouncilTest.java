package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElrondOfTheWhiteCouncil.class, GrizzlyBears.class, JaceBeleren.class, TheValeyard.class})
class ElrondOfTheWhiteCouncilTest extends BaseCardTest {

    @Test
    void fellowshipStealsChosenCreatureAndAidCountersAllControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears firstCard = new GrizzlyBears();
        firstCard.setOwnerId(player2.getId());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, firstCard);
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castElrond();

        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);
        harness.handleListChoice(player2, ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstOpponentCreature.getId(), secondOpponentCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstOpponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstOpponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondOpponentCreature);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, firstOpponentCreature, player2.getId())).isFalse();
    }

    private void castElrond() {
        harness.castFromHand(player1, new ElrondOfTheWhiteCouncil(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice vote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(vote).isNotNull();
        assertThat(vote.options()).containsExactlyElementsOf(
                ChoiceContext.ElrondOfTheWhiteCouncilChoice.OPTIONS);
    }

    @Test
    void twoAidVotesCounterAllControlledCreaturesButNotOpponents() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castElrond();
        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);
        harness.handleListChoice(player2, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fellowshipVoterWithoutCreaturesDoesNotPreventAid() {
        castElrond();
        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);
        harness.handleListChoice(player2, ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    void additionalVoteCanBeCastBeforeTheOpponentVotes() {
        harness.addToBattlefield(player1, new TheValeyard());
        castElrond();
        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);

        PendingInteraction.ColorChoice secondVote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondVote).isNotNull();
        assertThat(secondVote.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);
        harness.handleListChoice(player2, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);

        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3));
    }

    @Test
    void fellowshipChoicesStartWithTheActivePlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.enterBattlefieldAndReturn(player1, new ElrondOfTheWhiteCouncil());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice vote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(vote).isNotNull();
        harness.handleListChoice(vote.playerId().equals(player1.getId()) ? player1 : player2,
                ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP);
        vote = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(vote).isNotNull();
        harness.handleListChoice(vote.playerId().equals(player1.getId()) ? player1 : player2,
                ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
    }

    @Test
    void fellowshipCreatureCanAttackItsOwnersPlaneswalker() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        castElrond();
        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);
        harness.handleListChoice(player2, ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, creature, planeswalker.getId())).isTrue();
    }
}
