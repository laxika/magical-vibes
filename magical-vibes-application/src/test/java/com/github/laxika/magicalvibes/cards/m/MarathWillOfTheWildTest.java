package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarathWillOfTheWild.class, GrizzlyBears.class})
class MarathWillOfTheWildTest extends BaseCardTest {

    @Test
    void entersWithCountersEqualToManaSpentToCast() {
        harness.setHand(player1, List.of(new MarathWillOfTheWild()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Marath, Will of the Wild")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void putsXCountersOnTargetCreature() {
        Permanent marath = addCreatureReady(player1, new MarathWillOfTheWild());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        marath.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put X +1/+1 counters on target creature");

        assertThat(marath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void dealsXDamageToAnyTarget() {
        Permanent marath = addCreatureReady(player1, new MarathWillOfTheWild());
        marath.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Marath deals X damage to any target");

        assertThat(marath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void createsXByXElementalToken() {
        Permanent marath = addCreatureReady(player1, new MarathWillOfTheWild());
        marath.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create an X/X green Elemental creature token");

        assertThat(marath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
}
