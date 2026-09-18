package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DonnieAprilAdorkableDuo.class, Divination.class, Forest.class,
        LeoninScimitar.class, GrizzlyBears.class})
class DonnieAprilAdorkableDuoTest extends BaseCardTest {

    private static final String DRAW_MODE = "Target player draws two cards.";
    private static final String RETURN_MODE =
            "Target player returns an artifact, instant, or sorcery card from their graveyard to their hand.";

    @Test
    void bothModesTargetDifferentPlayersAndReturnOnlyAnEligibleCard() {
        Card drawnCard = new Forest();
        Card returnedCard = new Divination();
        Card otherEligibleCard = new LeoninScimitar();
        Card ineligibleCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player2, List.of(returnedCard, otherEligibleCard, ineligibleCard));

        castDuo();
        harness.handleListChoice(player1, DRAW_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(returnedCard.getId(), otherEligibleCard.getId());

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(returnedCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(otherEligibleCard, ineligibleCard);
    }

    private void castDuo() {
        harness.setHand(player1, List.of(new DonnieAprilAdorkableDuo()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
