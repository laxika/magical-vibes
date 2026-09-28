package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LutriPauperOtter.class, GrizzlyBears.class, Island.class})
class LutriPauperOtterTest extends BaseCardTest {

    @Test
    void enteringDiscardsHandAndDrawsThreeCards() {
        LutriPauperOtter lutri = new LutriPauperOtter();
        GrizzlyBears discarded = new GrizzlyBears();
        List<Card> drawn = List.of(new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(lutri, discarded));
        harness.setLibrary(player1, drawn);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }
}
