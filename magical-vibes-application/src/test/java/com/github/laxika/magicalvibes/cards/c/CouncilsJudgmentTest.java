package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilsJudgment.class, GrizzlyBears.class, Forest.class})
class CouncilsJudgmentTest extends BaseCardTest {

    @Test
    void exilesPermanentsTiedForMostVotes() {
        Permanent player1VotedFor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Unvoted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2VotedFor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Unvoted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new CouncilsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
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
        harness.setHand(player1, List.of(new CouncilsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }
}
