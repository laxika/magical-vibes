package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyclopeanMummy.class, WrathOfGod.class})
class CyclopeanMummyTest extends BaseCardTest {

    @Test
    @DisplayName("When Cyclopean Mummy dies, it is exiled instead of staying in the graveyard")
    void diesGoesToExile() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new CyclopeanMummy());
        Card mummyCard = mummy.getCard();

        // Player 1 wraths the board — Cyclopean Mummy dies and its ON_DEATH trigger goes on the stack.
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves — Mummy dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — exile from graveyard

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(mummyCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(mummyCard.getId()));
    }
}
