package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTenthDoctor.class, Forest.class, GrizzlyBears.class})
class TheTenthDoctorTest extends BaseCardTest {

    @Test
    void attackingExilesUntilNonlandAndSuspendsTheFoundCard() {
        addCreatureReady(player1, new TheTenthDoctor());
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, nonland));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .containsExactly(land, nonland);
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 3);
    }

    @Test
    void timeTravelCanAddCountersAcrossAllThreeEvents() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "SKIP");
        harness.handleListChoice(player1, "SKIP");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void timeTravelCanRemoveTheLastCounterFromAControlledPermanent() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }
}
