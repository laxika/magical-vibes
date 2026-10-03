package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AcrobaticManeuver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BomatCourier.class, GrizzlyBears.class, AcrobaticManeuver.class})
class BomatCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card of its controller's library face down when it attacks")
    void attacksExilesTopCardFaceDown() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card topCard = new GrizzlyBears();
        Card remainingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, remainingCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId()))
                .extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.getExiledWithPermanentEntries(courier.getId(), courier.getCard().getId()))
                .allMatch(entry -> entry.faceDown());
    }

    @Test
    @DisplayName("Returns its exiled cards to their owners' hands after paying the ability's costs")
    void sacrificeReturnsExiledCardsAndDiscardsHand() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card exiledCard = new GrizzlyBears();
        Card discardedCard = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledCard, courier.getId());
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(courier.getCard());
    }

    @Test
    void emptyLibraryDoesNotCauseDrawLossWhenAttacking() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(courier.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void emptyHandCanPaySacrificeAbilityWithNoExiledCards() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(courier);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(courier.getCard());
    }

    @Test
    void costsArePaidBeforeReturningCardsAndOtherCouriersCacheIsUntouched() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Permanent otherCourier = addCreatureReady(player1, new BomatCourier());
        Card firstCard = new BomatCourier();
        Card secondCard = new BomatCourier();
        Card otherCard = new BomatCourier();
        Card discardedCard = new BomatCourier();
        gd.addToExile(player1.getId(), firstCard, courier.getId(), true);
        gd.addToExile(player1.getId(), secondCard, courier.getId(), true);
        gd.addToExile(player1.getId(), otherCard, otherCourier.getId(), true);
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(courier);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard, courier.getCard());
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).containsExactly(firstCard, secondCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstCard, secondCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(otherCourier.getId())).containsExactly(otherCard);
    }

    @Test
    void sacrificingBeforeAttackTriggerResolvesLeavesTheLaterCardExiled() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card topCard = new BomatCourier();
        Card remainingCard = new BomatCourier();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).containsExactly(topCard);
        assertThat(gd.getExiledWithPermanentEntries(courier.getId(), courier.getCard().getId()))
                .allMatch(entry -> entry.faceDown());
    }

    @Test
    void flickeredCourierCannotRetrieveCardFromItsPreviousObjectsPendingAttack() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card drawnCard = new BomatCourier();
        Card exiledCard = new BomatCourier();
        Card remainingCard = new BomatCourier();
        harness.setLibrary(player1, List.of(drawnCard, exiledCard, remainingCard));
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0, courier.getId());
        resolveAllTriggers();

        Permanent returnedCourier = findPermanent(player1, "Bomat Courier");
        assertThat(returnedCourier.getId()).isNotEqualTo(courier.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).containsExactly(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(returnedCourier.getId())).isEmpty();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).containsExactly(exiledCard);
    }

    @Test
    void returnsCardsToTheirOwnersWhenCacheContainsBothPlayersCards() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card ownCard = new BomatCourier();
        Card opponentsCard = new BomatCourier();
        gd.addToExile(player1.getId(), ownCard, courier.getId(), true);
        gd.addToExile(player2.getId(), opponentsCard, courier.getId(), true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).isEmpty();
    }
}
