package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizenedArbiter.class, SerraAngel.class, GrizzlyBears.class})
class WizenedArbiterTest extends BaseCardTest {

    @Test
    void exchangesAWhiteOutsideGameCardForAHandCard() {
        Card outsideCard = new SerraAngel();
        Card handCard = new GrizzlyBears();
        Card nonWhiteCard = new GrizzlyBears();
        setSideboard(outsideCard, nonWhiteCard);

        castWizenedArbiter(handCard);

        PendingInteraction.ExchangeOutsideGameCardChoice outsideChoice =
                gd.interaction.activeInteraction(PendingInteraction.ExchangeOutsideGameCardChoice.class);
        assertThat(outsideChoice.cards()).containsExactly(outsideCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(outsideCard.getId())));

        PendingInteraction.ExchangeOutsideGameHandChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.ExchangeOutsideGameHandChoice.class);
        assertThat(handChoice.validIndices()).containsExactly(0);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardIndexChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(outsideCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonWhiteCard, handCard);
    }

    @Test
    void mayDeclineToRevealAnOutsideGameCard() {
        Card outsideCard = new SerraAngel();
        Card handCard = new GrizzlyBears();
        setSideboard(outsideCard);

        castWizenedArbiter(handCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardsChosen(List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCard);
    }

    @Test
    void onlyOffersWhiteOutsideGameCards() {
        Card nonWhiteCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        setSideboard(nonWhiteCard);

        castWizenedArbiter(handCard);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonWhiteCard);
    }

    @Test
    void doesNotOfferExchangeWithoutAHandCard() {
        Card outsideCard = new SerraAngel();
        setSideboard(outsideCard);

        harness.setHand(player1, List.of(new WizenedArbiter()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCard);
    }

    private void castWizenedArbiter(Card handCard) {
        harness.setHand(player1, List.of(new WizenedArbiter(), handCard));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setSideboard(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }
}
