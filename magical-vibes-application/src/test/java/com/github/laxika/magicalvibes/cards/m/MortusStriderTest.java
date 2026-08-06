package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MortusStriderTest extends BaseCardTest {

    @Test
    @DisplayName("When Mortus Strider dies, it returns to its owner's hand instead of staying in the graveyard")
    void diesReturnsToOwnersHand() {
        harness.addToBattlefield(player1, new MortusStrider());
        Permanent strider = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card striderCard = strider.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Strider dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — return to hand

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(striderCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(striderCard.getId()));
    }
}
