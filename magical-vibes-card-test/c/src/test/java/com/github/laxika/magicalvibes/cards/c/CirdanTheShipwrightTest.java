package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrapTheTrespassers;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CirdanTheShipwright.class, Forest.class, TrapTheTrespassers.class, Abundance.class})
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

        addCreatureReady(player1, new CirdanTheShipwright());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        voteFor(player1, player1);
        voteFor(player2, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondDraw);
    }

    @Test
    void noVotePlayerMayDeclinePuttingAPermanentOntoTheBattlefield() {
        Forest permanent = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(permanent));
        harness.enterBattlefieldAndReturn(player1, new CirdanTheShipwright());
        harness.passBothPriorities();

        voteFor(player1, player1);
        voteFor(player2, player1);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyPermanentCardsAreOfferedToTheNoVotePlayer() {
        Forest permanent = new Forest();
        TrapTheTrespassers instant = new TrapTheTrespassers();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(instant, permanent));
        harness.enterBattlefieldAndReturn(player1, new CirdanTheShipwright());
        harness.passBothPriorities();

        voteFor(player1, player1);
        voteFor(player2, player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validCardIds()).containsExactly(permanent.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(permanent.getId()));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
    }

    @Test
    void drawsStartWithTheActivePlayerEvenWhenCirdanHasAnotherController() {
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new CirdanTheShipwright());
        harness.passBothPriorities();

        voteFor(player1, player1);
        voteFor(player2, player2);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(text -> text.equals(player1.getUsername() + " draws a card.")
                        || text.equals(player2.getUsername() + " draws a card.")))
                .containsExactly(player2.getUsername() + " draws a card.",
                        player1.getUsername() + " draws a card.");
    }

    @Test
    void drawReplacementDecisionsFinishBeforeTheNoVotePlayerChoosesAPermanent() {
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new CirdanTheShipwright());
        harness.passBothPriorities();

        voteFor(player1, player1);
        voteFor(player2, player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of());
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
