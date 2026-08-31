package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaviorOfTheSleeping.class, GloriousAnthem.class, GrizzlyBears.class})
class SaviorOfTheSleepingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an enchantment you control dies")
    void controlledEnchantmentDyingAddsCounter() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        putIntoGraveyard(anthem);

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment or a creature you control")
    void ignoresOpponentEnchantmentAndCreature() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent opponentAnthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        putIntoGraveyard(opponentAnthem);
        putIntoGraveyard(creature);

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
