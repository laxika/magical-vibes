package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cyclone.class, GrizzlyBears.class, HillGiant.class, Disenchant.class, Solemnity.class})
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

    @Test
    @DisplayName("Cyclone does not trigger during its opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(cyclone.getCounterCount(CounterType.WIND)).isZero();
        harness.assertOnBattlefield(player1, "Cyclone");
    }

    @Test
    @DisplayName("Insufficient green mana sacrifices Cyclone without dealing damage")
    void insufficientManaSacrificesWithoutDamage() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        cyclone.setCounterCount(CounterType.WIND, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Cyclone");
        harness.assertInGraveyard(player1, "Cyclone");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two wind counters deal lethal damage to creatures on both sides")
    void lethalDamageDestroysCreaturesOnBothSides() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        cyclone.setCounterCount(CounterType.WIND, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cyclone");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A removed Cyclone still allows payment and deals its last known wind-counter damage")
    void removedCycloneUsesLastKnownCounters() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        cyclone.setCounterCount(CounterType.WIND, 2);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Disenchant()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, cyclone.getId());
        harness.assertInGraveyard(player1, "Cyclone");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The controller may decline a zero-cost payment when wind counters cannot be placed")
    void mayDeclinePaymentWithZeroWindCounters() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new Cyclone());
        harness.addToBattlefield(player2, new Solemnity());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(cyclone.getCounterCount(CounterType.WIND)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Cyclone");
        harness.assertInGraveyard(player1, "Cyclone");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
