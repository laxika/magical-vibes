package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AragornAndArwenWed.class, GrizzlyBears.class})
class AragornAndArwenWedTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts counters on other creatures and gains life for each one")
    void enteringCountersAndGainsLife() {
        Permanent firstOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        castAragornAndArwen();

        assertThat(firstOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("The entering creature does not count itself")
    void enteringCreatureDoesNotCountItself() {
        harness.setLife(player1, 10);
        Permanent aragornAndArwen = castAragornAndArwen();

        assertThat(aragornAndArwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Attacking puts counters on other creatures and gains life for each one")
    void attackingCountersAndGainsLife() {
        Permanent aragornAndArwen = harness.addToBattlefieldAndReturn(player1, new AragornAndArwenWed());
        aragornAndArwen.setSummoningSick(false);
        Permanent firstOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(firstOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 12);
    }

    private Permanent castAragornAndArwen() {
        harness.setHand(player1, List.of(new AragornAndArwenWed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AragornAndArwenWed)
                .findFirst()
                .orElseThrow();
    }
}
