package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({StockingThePantry.class, KujarSeedsculptor.class, GrizzlyBears.class,
        ZimoneParadoxSculptor.class, DarksteelCitadel.class})
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
}
