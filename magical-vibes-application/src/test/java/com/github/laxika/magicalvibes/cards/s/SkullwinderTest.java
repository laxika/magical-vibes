package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skullwinder.class, Forest.class, HolyDay.class})
class SkullwinderTest extends BaseCardTest {

    @Test
    void returnsTargetedOwnCardThenOpponentReturnsChosenCard() {
        Card ownCard = new HolyDay();
        Card opponentCard = new Forest();
        Card otherOpponentCard = new HolyDay();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard, otherOpponentCard));
        castAndResolveEtb();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCard.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(opponentCard.getId(), otherOpponentCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherOpponentCard);
    }

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skullwinder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
