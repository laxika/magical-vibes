package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CycloneTest extends BaseCardTest {

    @Test
    @DisplayName("Paying for the wind counters deals damage to each creature and player")
    void payingDealsDamageToAllCreaturesAndPlayers() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cyclone.getCounterCount(CounterType.WIND)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears2);
        assertThat(bears1.getMarkedDamage()).isEqualTo(1);
        assertThat(bears2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the payment sacrifices Cyclone")
    void decliningPaymentSacrificesCyclone() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cyclone);
        harness.assertInGraveyard(player1, "Cyclone");
    }

    @Test
    @DisplayName("The payment increases with each wind counter")
    void paymentIncreasesWithWindCounters() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        cyclone.setCounterCount(CounterType.WIND, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(cyclone.getCounterCount(CounterType.WIND)).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cyclone);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
