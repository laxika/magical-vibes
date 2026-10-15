package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skullwinder.class, Forest.class})
class SkullwinderTest extends BaseCardTest {

    @Test
    void returnsTargetedOwnCardThenOpponentReturnsChosenCard() {
        Card ownCard = new Skullwinder();
        Card opponentCard = new Forest();
        Card otherOpponentCard = new Skullwinder();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard, otherOpponentCard));
        castAndResolveEtb();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        chooseOpponentIfPrompted();

        PendingInteraction.GraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(opponentCard.getId(), otherOpponentCard.getId());

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherOpponentCard);
    }

    @Test
    void canChooseOpponentWithEmptyGraveyard() {
        Card ownCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());
        castAndResolveEtb();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);
        chooseOpponentIfPrompted();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsOpponentsOnlyGraveyardCard() {
        Card ownCard = new Forest();
        Card opponentCard = new Skullwinder();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castAndResolveEtb();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);
        chooseOpponentIfPrompted();

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyOwnGraveyardDoesNotAllowOpponentToReturnCard() {
        Card opponentCard = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCard));
        castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    void illegalGraveyardTargetPreventsAllReturns() {
        Card ownCard = new Forest();
        Card opponentCard = new Skullwinder();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castAndResolveEtb();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCard.getId()));
        chooseOpponentIfPrompted();
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(ownCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void chooseOpponentIfPrompted() {
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Skullwinder(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
