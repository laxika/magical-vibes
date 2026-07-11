package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndyingBeastTest extends BaseCardTest {

    @Test
    @DisplayName("When Undying Beast dies, it is put on top of its owner's library instead of staying in the graveyard")
    void diesGoesToTopOfLibrary() {
        harness.addToBattlefield(player1, new UndyingBeast());
        Permanent beast = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card beastCard = beast.getCard();

        // Player 1 wraths the board — Undying Beast dies and its ON_DEATH trigger goes on the stack.
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Beast dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — tuck on top of library

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(beastCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(beastCard.getId()));
    }
}
