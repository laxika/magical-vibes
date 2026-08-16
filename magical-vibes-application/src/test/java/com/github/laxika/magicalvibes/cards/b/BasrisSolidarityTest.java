package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasrisSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control")
    void putsCounterOnEachCreatureYouControl() {
        Permanent firstBear = new Permanent(new GrizzlyBears());
        Permanent secondBear = new Permanent(new GrizzlyBears());
        Card noncreature = new Card();
        noncreature.setType(CardType.ENCHANTMENT);
        Permanent ownNoncreature = new Permanent(noncreature);
        Permanent opponentBear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(firstBear, secondBear, ownNoncreature));
        gd.playerBattlefields.get(player2.getId()).add(opponentBear);

        harness.setHand(player1, List.of(new BasrisSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownNoncreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
