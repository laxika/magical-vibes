package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenGoblinRevenant.class, Forest.class, Mountain.class, WildGuess.class})
class GreenGoblinRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking discards a card, then draws for every card discarded this turn")
    void attackingCountsAllDiscardsThisTurn() {
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        addCreatureReady(player1, new GreenGoblinRevenant());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
