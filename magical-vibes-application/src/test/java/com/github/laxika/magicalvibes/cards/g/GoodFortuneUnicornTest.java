package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoodFortuneUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control gets a +1/+1 counter")
    void anotherAllyCreatureEnteringGetsCounter() {
        harness.addToBattlefield(player1, new GoodFortuneUnicorn());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature entering does not get a counter")
    void opponentCreatureEnteringDoesNotGetCounter() {
        harness.addToBattlefield(player1, new GoodFortuneUnicorn());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent enteringCreature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Good-Fortune Unicorn's own entry does not trigger its ability")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new GoodFortuneUnicorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
