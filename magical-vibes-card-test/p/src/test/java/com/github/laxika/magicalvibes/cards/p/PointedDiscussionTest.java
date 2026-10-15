package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PointedDiscussion.class, Forest.class, GrizzlyBears.class})
class PointedDiscussionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, loses 2 life, and creates a Blood token")
    void drawsLosesLifeAndCreatesBlood() {
        Forest firstDrawnCard = new Forest();
        GrizzlyBears secondDrawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.setHand(player1, List.of(new PointedDiscussion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard, secondDrawnCard);
        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Blood")).isOne();
        harness.assertInGraveyard(player1, "Pointed Discussion");
    }

    @Test
    @DisplayName("Only the caster draws, loses life, and receives Blood")
    void leavesOpponentUnaffected() {
        Forest firstDrawnCard = new Forest();
        Forest secondDrawnCard = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new PointedDiscussion()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 3);
        harness.setLife(player2, 13);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard, secondDrawnCard);
        harness.assertLife(player1, 1);
        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(countPermanents(player1, "Blood")).isOne();
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    @DisplayName("Newly created Blood can immediately discard and sacrifice to draw")
    void bloodCanImmediatelyBeActivated() {
        Forest discardedCard = new Forest();
        Forest retainedCard = new Forest();
        Forest drawnWithBlood = new Forest();
        harness.setLibrary(player1, List.of(discardedCard, retainedCard, drawnWithBlood));
        harness.setHand(player1, List.of(new PointedDiscussion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        var blood = findPermanent(player1, "Blood");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard, drawnWithBlood);
        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Blood")).isZero();
    }
}
