package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeatherseedTreefolk.class, WrathOfGod.class})
class WeatherseedTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("When Weatherseed Treefolk dies, it returns to its owner's hand")
    void diesReturnsToOwnersHand() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new WeatherseedTreefolk());
        Card treefolkCard = treefolk.getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(treefolkCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(treefolkCard.getId()));
    }

    @Test
    @DisplayName("When controlled by another player, it returns to its owner's hand")
    void diesReturnsToOwnersHandWhenControlledByOpponent() {
        WeatherseedTreefolk treefolkCard = new WeatherseedTreefolk();
        treefolkCard.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, treefolkCard);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(treefolkCard.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(treefolkCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(treefolkCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(treefolkCard.getId()));
    }

    @Test
    @DisplayName("Lethal damage puts Treefolk in the graveyard until its death trigger resolves")
    void lethalDamageReturnsOnlyWhenTriggerResolves() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new WeatherseedTreefolk());
        treefolk.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Weatherseed Treefolk");
        harness.assertInGraveyard(player1, "Weatherseed Treefolk");
        harness.assertNotInHand(player1, "Weatherseed Treefolk");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(treefolk.getCard());
        harness.assertNotInGraveyard(player1, "Weatherseed Treefolk");
    }

    @Test
    @DisplayName("Simultaneously dying Treefolk each return to their respective owners")
    void simultaneousDeathsReturnEachCardToItsOwner() {
        WeatherseedTreefolk first = new WeatherseedTreefolk();
        WeatherseedTreefolk second = new WeatherseedTreefolk();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player2, second);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(second);
        harness.assertNotInHand(player1, "Weatherseed Treefolk");
        harness.assertNotInHand(player2, "Weatherseed Treefolk");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerHands.get(player2.getId())).contains(second).doesNotContain(first);
        harness.assertNotInGraveyard(player1, "Weatherseed Treefolk");
        harness.assertNotInGraveyard(player2, "Weatherseed Treefolk");
    }
}
