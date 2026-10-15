package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Battlegrowth;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.cards.z.ZimoneParadoxSculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StockingThePantry.class, KujarSeedsculptor.class, GrizzlyBears.class,
        ZimoneParadoxSculptor.class, DarksteelCitadel.class, Battlegrowth.class})
class StockingThePantryTest extends BaseCardTest {

    @Test
    void putsSupplyCounterWhenYouPutPlusOneCountersOnControlledCreature() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isEqualTo(1);
    }

    @Test
    void removesSupplyCounterAndDrawsCard() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        pantry.setCounterCount(CounterType.SUPPLY, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void doesNotPutSupplyCounterWhenCountersArePutOnControlledNoncreature() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneParadoxSculptor());
        zimone.setSummoningSick(false);
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        citadel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int zimoneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zimone);
        harness.activateAbilityWithMultiTargets(player1, zimoneIndex, 0, List.of(citadel.getId()));
        harness.passBothPriorities();

        assertThat(citadel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
    }

    @Test
    void doesNotTriggerWhenOpponentPutsCounterOnYourCreature() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Battlegrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
    }

    @Test
    void doesNotTriggerWhenYouPutCounterOnOpponentsCreature() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
    }

    @Test
    void putsOneSupplyCounterPerCreatureRatherThanPerCounter() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneParadoxSculptor());
        zimone.setSummoningSick(false);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutSupplyCounter() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void supplyCounterIsPaidImmediatelyAndCannotBeSpentAgain() {
        Permanent pantry = harness.addToBattlefieldAndReturn(player1, new StockingThePantry());
        pantry.setCounterCount(CounterType.SUPPLY, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(pantry.getCounterCount(CounterType.SUPPLY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
