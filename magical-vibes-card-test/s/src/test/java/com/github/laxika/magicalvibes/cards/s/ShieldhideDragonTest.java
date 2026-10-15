package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldhideDragon.class, DromokaWarrior.class})
class ShieldhideDragonTest extends BaseCardTest {

    @Test
    void megamorphCountersOtherDragonsYouControl() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new ShieldhideDragon());
        Permanent opposingDragon = harness.addToBattlefieldAndReturn(player2, new ShieldhideDragon());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        Permanent shieldhideDragon = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shieldhideDragon));
        harness.passBothPriorities();

        assertThat(shieldhideDragon.isFaceDown()).isFalse();
        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shieldhideDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphCountersOnlyOtherDragons() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new ShieldhideDragon());
        Permanent dragon = castFaceDown();

        harness.inMutationScope(() -> gs.turnPermanentFaceUpWithoutPayingManaCost(gd, dragon));
        harness.passBothPriorities();

        assertThat(dragon.isFaceDown()).isFalse();
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotCounterAnyDragon() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new ShieldhideDragon());

        harness.castFromHand(player1, new ShieldhideDragon(), "{5}{W}");
        harness.passBothPriorities();

        Permanent dragon = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(dragon.isFaceDown()).isFalse();
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerChecksOtherDragonsAtResolutionAndMegamorphCounterIsImmediate() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new ShieldhideDragon());
        Permanent dragon = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragon));

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent lateDragon = harness.addToBattlefieldAndReturn(player1, new ShieldhideDragon());
        harness.passBothPriorities();

        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void faceUpCombatDamageGainsLife() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShieldhideDragon());
        dragon.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void faceDownCombatDamageDoesNotGainLife() {
        Permanent dragon = castFaceDown();
        dragon.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new ShieldhideDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
