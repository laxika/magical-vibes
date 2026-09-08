package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherseedTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("When Weatherseed Treefolk dies, it returns to its owner's hand")
    void diesReturnsToOwnersHand() {
        harness.addToBattlefield(player1, new WeatherseedTreefolk());
        Permanent treefolk = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card treefolkCard = treefolk.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(treefolkCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(treefolkCard.getId()));
    }
}
