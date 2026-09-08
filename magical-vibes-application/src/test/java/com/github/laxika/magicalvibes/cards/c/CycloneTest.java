package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CycloneTest extends BaseCardTest {

    @Test
    void payingItsUpkeepAddsAWindCounterAndDealsThatMuchDamageToCreaturesAndPlayers() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cyclone.getCounterCount(CounterType.WIND)).isEqualTo(1);
        assertThat(ownBears.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingBears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void windCounterCountScalesTheNextPaymentAndDamage() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        cyclone.setCounterCount(CounterType.WIND, 1);
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cyclone.getCounterCount(CounterType.WIND)).isEqualTo(2);
        assertThat(opposingBears.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void decliningItsUpkeepSacrificesItWithoutDealingDamage() {
        harness.addToBattlefield(player1, new Cyclone());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Cyclone");
        assertThat(opposingBears.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
