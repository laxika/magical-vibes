package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AllWillBeOne;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HardbristleBandit;
import com.github.laxika.magicalvibes.cards.o.OzolithTheShatteredSpire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BristlyBillSpineSower.class, Forest.class, HardbristleBandit.class,
        OzolithTheShatteredSpire.class, Blightbeetle.class, AllWillBeOne.class})
class BristlyBillSpineSowerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on target creature")
    void landfallPutsCounterOnTargetCreature() {
        addCreatureReady(player1, new BristlyBillSpineSower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HardbristleBandit());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall cannot target a noncreature permanent")
    void landfallRejectsNoncreatureTarget() {
        addCreatureReady(player1, new BristlyBillSpineSower());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The activated ability doubles +1/+1 counters on controlled creatures")
    void activationDoublesControlledCreatureCounters() {
        addCreatureReady(player1, new BristlyBillSpineSower());
        Permanent ownCreature = addCreatureReady(player1, new HardbristleBandit());
        Permanent opponentCreature = addCreatureReady(player2, new HardbristleBandit());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void landfallCanTargetBillItself() {
        Permanent bill = harness.addToBattlefieldAndReturn(player1, new BristlyBillSpineSower());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bill.getId());
        harness.passBothPriorities();

        assertThat(bill.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent bill = harness.addToBattlefieldAndReturn(player1, new BristlyBillSpineSower());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bill.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activationUsesCreaturesAndCountersAtResolution() {
        Permanent bill = harness.addToBattlefieldAndReturn(player1, new BristlyBillSpineSower());
        bill.setTapped(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        newcomer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bill.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void activationResolvesAfterBillLeavesBattlefield() {
        Permanent bill = harness.addToBattlefieldAndReturn(player1, new BristlyBillSpineSower());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(bill);
        gd.playerGraveyards.get(player1.getId()).add(bill.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @CardUsed({BristlyBillSpineSower.class, HardbristleBandit.class, OzolithTheShatteredSpire.class})
    void doublingAppliesCounterPlacementReplacement() {
        harness.addToBattlefield(player1, new BristlyBillSpineSower());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        harness.addToBattlefield(player1, new OzolithTheShatteredSpire());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @CardUsed({BristlyBillSpineSower.class, HardbristleBandit.class, Blightbeetle.class})
    void doublingRespectsProhibitionOnPlusOneCounters() {
        harness.addToBattlefield(player1, new BristlyBillSpineSower());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player2, new Blightbeetle());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({BristlyBillSpineSower.class, HardbristleBandit.class, AllWillBeOne.class})
    void doublingTriggersAllWillBeOneForCountersAdded() {
        harness.addToBattlefield(player1, new BristlyBillSpineSower());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new AllWillBeOne());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.assertLife(player2, 17);
    }
}
