package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CirdanTheShipwright.class, Forest.class})
class CirdanTheShipwrightTest extends BaseCardTest {

    @Test
    void votesGiveDrawsAndNoVotePlayersMayPutPermanentsFromHandOntoBattlefield() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        Forest freePermanent = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(freePermanent));
        harness.enterBattlefieldAndReturn(player1, new CirdanTheShipwright());
        harness.passBothPriorities();

        voteFor(player1, player1);
        voteFor(player2, player1);

        PendingInteraction.MultiPermanentChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(handChoice).isNotNull();
        assertThat(handChoice.playerId()).isEqualTo(player2.getId());
        assertThat(handChoice.validCardIds()).containsExactly(freePermanent.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(freePermanent.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(freePermanent.getId());
    }

    @Test
    void attackTriggerAlsoUsesTheSecretVote() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw));
        harness.setLibrary(player2, List.of(secondDraw));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        Permanent cirdan = harness.addToBattlefieldAndReturn(player1, new CirdanTheShipwright());
        cirdan.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        voteFor(player1, player1);
        voteFor(player2, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondDraw);
    }

    private void voteFor(Player voter, Player votedFor) {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(voter.getId());
        assertThat(choice.validPlayerIds()).contains(votedFor.getId());
        harness.handleMultiplePermanentsChosen(voter, List.of(votedFor.getId()));
    }
}
