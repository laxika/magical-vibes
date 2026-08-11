package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SquadCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other creature its controller controls")
    void entersWithCountersForOtherControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSquadCaptain();

        Permanent captain = findPermanent(player1, "Squad Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when it is the only creature its controller controls")
    void doesNotCountItself() {
        castSquadCaptain();

        Permanent captain = findPermanent(player1, "Squad Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSquadCaptain() {
        harness.setHand(player1, List.of(new SquadCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
