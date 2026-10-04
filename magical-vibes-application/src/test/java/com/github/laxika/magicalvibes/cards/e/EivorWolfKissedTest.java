package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalladOfTheBlackFlag;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EivorWolfKissed.class, BalladOfTheBlackFlag.class, Mountain.class, Murder.class, RestInPeace.class})
class EivorWolfKissedTest extends BaseCardTest {

    @Test
    void millsCombatDamageAndReturnsOneSagaAndOneLand() {
        addAttackingEivor();
        Card saga = sagaCard();
        Card land = landCard();
        List<Card> invalidCards = List.of(plainCard("Invalid 1"), plainCard("Invalid 2"),
                plainCard("Invalid 3"), plainCard("Invalid 4"), plainCard("Invalid 5"));
        harness.setLibrary(player1, List.of(saga, land, invalidCards.get(0), invalidCards.get(1),
                invalidCards.get(2), invalidCards.get(3), invalidCards.get(4)));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice sagaChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(sagaChoice.validCardIds()).containsExactly(saga.getId());
        harness.handleMultipleCardsChosen(player1, List.of(saga.getId()));

        PendingInteraction.MultiGraveyardChoice landChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(landChoice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Test Saga");
        harness.assertOnBattlefield(player1, "Test Land");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(invalidCards);
    }

    @Test
    void mayDeclineTheSagaAndLandChoices() {
        addAttackingEivor();
        Card saga = sagaCard();
        Card land = landCard();
        harness.setLibrary(player1, List.of(saga, land));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Test Saga");
        harness.assertNotOnBattlefield(player1, "Test Land");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(saga, land);
    }

    @Test
    void mayReturnOnlyTheLandWhileDecliningTheSaga() {
        addAttackingEivor();
        Card saga = new BalladOfTheBlackFlag();
        Card land = new Mountain();
        harness.setLibrary(player1, List.of(saga, land));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Ballad of the Black Flag");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(saga);
        assertThat(findPermanent(player1, "Mountain").isTapped()).isFalse();
    }

    @Test
    void mayReturnOnlyTheSagaWhileDecliningTheLand() {
        addAttackingEivor();
        Card saga = new BalladOfTheBlackFlag();
        Card land = new Mountain();
        harness.setLibrary(player1, List.of(saga, land));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(saga.getId()));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ballad of the Black Flag");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
    }

    @Test
    void millsExactlyTheDamageAndCannotReturnPreviouslyMilledCards() {
        addAttackingEivor();
        Card oldSaga = new BalladOfTheBlackFlag();
        Card oldLand = new Mountain();
        harness.setGraveyard(player1, List.of(oldSaga, oldLand));
        List<Card> milled = List.of(new Murder(), new Murder(), new Murder(), new Murder(),
                new Murder(), new Murder(), new Murder());
        Card eighthCard = new Mountain();
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2),
                milled.get(3), milled.get(4), milled.get(5), milled.get(6), eighthCard));
        Card opponentsCard = new Mountain();
        harness.setLibrary(player2, List.of(opponentsCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eighthCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(milled)
                .contains(oldSaga, oldLand).hasSize(9);
        harness.assertNotOnBattlefield(player1, "Ballad of the Black Flag");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    void mayReturnSagaAndLandExiledByAMillReplacementEffect() {
        addAttackingEivor();
        harness.addToBattlefield(player2, new RestInPeace());
        Card saga = new BalladOfTheBlackFlag();
        Card land = new Mountain();
        harness.setLibrary(player1, List.of(saga, land));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice sagaChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(sagaChoice).isNotNull();
        assertThat(sagaChoice.validCardIds()).containsExactly(saga.getId());
        harness.handleMultipleCardsChosen(player1, List.of(saga.getId()));
        PendingInteraction.MultiGraveyardChoice landChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(landChoice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ballad of the Black Flag");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.findExiledCard(saga.getId())).isNull();
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    private Permanent addAttackingEivor() {
        Permanent eivor = addCreatureReady(player1, new EivorWolfKissed());
        eivor.setAttacking(true);
        return eivor;
    }

    private Card sagaCard() {
        Card saga = new Card();
        saga.setName("Test Saga");
        saga.setType(CardType.ENCHANTMENT);
        saga.setSubtypes(List.of(CardSubtype.SAGA));
        return saga;
    }

    private Card landCard() {
        Card land = new Card();
        land.setName("Test Land");
        land.setType(CardType.LAND);
        return land;
    }

    private Card plainCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        return card;
    }
}
