package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfAncientLore.class, GrizzlyBears.class})
class PriestOfAncientLoreTest extends BaseCardTest {

    @Test
    void entersGainsLifeAndDrawsCard() {
        Card drawnCard = new GrizzlyBears();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PriestOfAncientLore()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
