package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Blastoderm;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilsJudgment.class, GrizzlyBears.class, Forest.class, Blastoderm.class, SolRing.class, TheValeyard.class})
class CouncilsJudgmentTest extends BaseCardTest {

    @Test
    void exilesPermanentsTiedForMostVotes() {
        Permanent player1VotedFor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Unvoted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2VotedFor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Unvoted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(player2VotedFor.getId(), player2Unvoted.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2VotedFor.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2VotedFor.getId(), player2Unvoted.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Unvoted.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(player1VotedFor, player1Unvoted, player1Land);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(player2Land)
                .doesNotContain(player2VotedFor, player2Unvoted);
    }

    @Test
    void automaticallyVotesWhenOnlyOneOpponentNonlandPermanentExists() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void unanimousVotesExileOnlyTheMostVotedPermanent() {
        Permanent voted = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent unvoted = harness.addToBattlefieldAndReturn(player2, new Blastoderm());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(voted.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(voted, unvoted);
        harness.handleMultiplePermanentsChosen(player2, List.of(voted.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(unvoted);
        assertThat(gd.playerExiledCards.get(player2.getId())).contains(voted.getCard());
        harness.assertNotInGraveyard(player2, "Sol Ring");
    }

    @Test
    void canVoteForAndExileAPermanentWithShroud() {
        Permanent shrouded = harness.addToBattlefieldAndReturn(player2, new Blastoderm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(shrouded.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(shrouded.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerExiledCards.get(player2.getId())).contains(shrouded.getCard());
        harness.assertNotInGraveyard(player2, "Blastoderm");
    }

    @Test
    void resolvesWithoutVotingWhenOnlyOpposingLandsExist() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingLand);
        harness.assertInGraveyard(player1, "Council's Judgment");
    }

    @Test
    void votingStartsWithTheCasterWhenPlayerTwoCasts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Blastoderm());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));
        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ownPermanent);
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(first.getCard(), second.getCard());
    }

    @Test
    void opponentWithTheValeyardMayCastAnAdditionalVote() {
        Permanent valeyard = harness.addToBattlefieldAndReturn(player2, new TheValeyard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(valeyard.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(valeyard, artifact);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }
}
