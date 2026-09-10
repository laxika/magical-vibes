package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WilderlandScrounger.class, GrizzlyBears.class})
class WilderlandScroungerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutLargeCreature() {
        Permanent scrounger = addCreatureReady(player1, new WilderlandScrounger());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(scrounger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When it attacks with ferocious, puts a +1/+1 counter on each creature you control")
    void triggersWithLargeCreature() {
        Permanent scrounger = addCreatureReady(player1, new WilderlandScrounger());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent largeCreature = addCreatureReady(player1, makeCreature("Large Creature", 4, 4));
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(scrounger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(largeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
