package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BelltollDragon.class)
class BelltollDragonTest extends BaseCardTest {

    @Test
    void megamorphCountersItAndOtherDragonsYouControlWhenTurnedFaceUp() {
        Permanent otherDragon = harness.addToBattlefieldAndReturn(player1, new BelltollDragon());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new BelltollDragon());
        harness.setHand(player1, List.of(new BelltollDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != otherDragon)
                .findFirst()
                .orElseThrow();
        assertThat(dragon.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragon));
        harness.passBothPriorities();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
