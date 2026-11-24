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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElrondOfTheWhiteCouncil.class, GrizzlyBears.class, JaceBeleren.class, TheValeyard.class})
class ElrondOfTheWhiteCouncilTest extends BaseCardTest {

    @Test
    @DisplayName("A fellowship voter's chosen creature is stolen and each aid vote counters your creatures")
    void fellowshipStealsChosenCreatureAndAidCountersAllControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears firstCard = new GrizzlyBears();
        firstCard.setOwnerId(player2.getId());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, firstCard);
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castElrond();

        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.FELLOWSHIP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstOpponentCreature.getId(), secondOpponentCreature.getId());

        harness.handlePermanentChosen(player2, firstOpponentCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstOpponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondOpponentCreature);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, firstOpponentCreature, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("A voter with two fellowship votes chooses once per vote and may choose the same creature twice")
    void twoFellowshipVotesChooseOncePerVote() {
        Permanent valeyard = harness.addToBattlefieldAndReturn(player2, new TheValeyard());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castElrond();
        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.FELLOWSHIP);
        harness.handleListChoice(player2, ChoiceContext.AdditionalVoteChoice.VOTE_AGAIN);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.FELLOWSHIP);

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(valeyard.getId(), first.getId(), second.getId());
        harness.handlePermanentChosen(player2, first.getId());

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(valeyard.getId(), first.getId(), second.getId());
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(valeyard, second);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castElrond() {
        harness.castFromHand(player1, new ElrondOfTheWhiteCouncil(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice vote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(vote).isNotNull();
        assertThat(vote.options()).containsExactlyElementsOf(
                List.of(ElrondOfTheWhiteCouncil.FELLOWSHIP, ElrondOfTheWhiteCouncil.AID));
    }

    @Test
    @DisplayName("Two aid votes put two counters on each creature you control and none on an opponent's")
    void twoAidVotesCounterAllControlledCreaturesButNotOpponents() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castElrond();
        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.AID);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A fellowship voter with no creature chooses nothing and aid still applies")
    void fellowshipVoterWithoutCreaturesDoesNotPreventAid() {
        castElrond();
        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.FELLOWSHIP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("An additional vote is cast right after the player's own vote")
    void additionalVoteCanBeCastBeforeTheOpponentVotes() {
        harness.addToBattlefield(player1, new TheValeyard());
        castElrond();
        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player1, ChoiceContext.AdditionalVoteChoice.VOTE_AGAIN);

        PendingInteraction.ColorChoice secondVote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondVote).isNotNull();
        assertThat(secondVote.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.AID);

        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3));
    }

    @Test
    @DisplayName("Fellowship choices start with the active player")
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
                ElrondOfTheWhiteCouncil.FELLOWSHIP);
        vote = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(vote).isNotNull();
        harness.handleListChoice(vote.playerId().equals(player1.getId()) ? player1 : player2,
                ElrondOfTheWhiteCouncil.FELLOWSHIP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A stolen creature can't attack its owner but can attack the owner's planeswalker")
    void fellowshipCreatureCanAttackItsOwnersPlaneswalker() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        castElrond();
        harness.handleListChoice(player1, ElrondOfTheWhiteCouncil.AID);
        harness.handleListChoice(player2, ElrondOfTheWhiteCouncil.FELLOWSHIP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, creature, planeswalker.getId())).isTrue();
    }
}
