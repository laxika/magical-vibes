package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StormcragElemental.class)
class StormcragElementalTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new StormcragElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Stormcrag Elemental");
        assertThat(elemental.isFaceDown()).isTrue();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental));

        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
