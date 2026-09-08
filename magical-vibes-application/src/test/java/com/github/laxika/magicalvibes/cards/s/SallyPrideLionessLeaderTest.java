package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SallyPrideLionessLeader.class, YoungPyromancer.class, Shock.class, GrizzlyBears.class})
class SallyPrideLionessLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with Mutant tokens equal to the number of nontoken creatures you control")
    void enteringCountsNontokenCreatures() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SallyPrideLionessLeader()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutant")).hasSize(2);
    }

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on each creature you control")
    void attackingCountersEachCreatureYouControl() {
        Permanent sally = addCreatureReady(player1, new SallyPrideLionessLeader());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(sally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
