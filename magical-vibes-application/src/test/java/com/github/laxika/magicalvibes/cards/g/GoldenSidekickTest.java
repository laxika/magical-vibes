package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenSidekick.class, AngelOfMercy.class, GrizzlyBears.class})
class GoldenSidekickTest extends BaseCardTest {

    @Test
    void lifeGainPerpetuallyBoostsARandomCreatureCardInHand() {
        harness.addToBattlefield(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand.stream().filter(card -> card.getPower() == 5 && card.getToughness() == 5).count())
                .isEqualTo(1);
        assertThat(hand.stream().filter(card -> card.getPower() == 2 && card.getToughness() == 2).count())
                .isEqualTo(1);
    }
}
