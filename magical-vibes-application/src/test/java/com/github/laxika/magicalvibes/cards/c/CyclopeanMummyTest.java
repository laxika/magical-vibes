package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CyclopeanMummyTest extends BaseCardTest {

    @Test
    @DisplayName("When Cyclopean Mummy dies, it is exiled instead of staying in the graveyard")
    void diesGoesToExile() {
        harness.addToBattlefield(player1, new CyclopeanMummy());
        Permanent mummy = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card mummyCard = mummy.getCard();

        // Player 1 wraths the board — Cyclopean Mummy dies and its ON_DEATH trigger goes on the stack.
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Mummy dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — exile from graveyard

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(mummyCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(mummyCard.getId()));
    }
}
