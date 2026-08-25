package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaucousEntertainer.class, GrizzlyBears.class})
class RaucousEntertainerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on creatures you control that entered this turn")
    void putsCountersOnCreaturesThatEnteredThisTurn() {
        Permanent entertainer = addReady(player1, new RaucousEntertainer());
        Permanent enteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                List.of(entertainer.getCard(), enteredCreature.getCard()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(entertainer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(enteredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(oldCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private Permanent addReady(Player player, RaucousEntertainer entertainer) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, entertainer);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
