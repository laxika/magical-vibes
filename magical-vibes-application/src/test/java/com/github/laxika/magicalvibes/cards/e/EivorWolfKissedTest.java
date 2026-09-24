package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EivorWolfKissed.class})
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
