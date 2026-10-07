package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormwingDragon.class, ColossodonYearling.class})
class StormwingDragonTest extends BaseCardTest {

    @Test
    void megamorphCountersOtherDragonsYouControl() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new StormwingDragon());
        Permanent opposingDragon = harness.addToBattlefieldAndReturn(player2, new StormwingDragon());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent stormwingDragon = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stormwingDragon));
        harness.passBothPriorities();

        assertThat(stormwingDragon.isFaceDown()).isFalse();
        assertThat(stormwingDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingFaceUpDoesNotPutCountersOnOtherDragons() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new StormwingDragon());
        harness.setHand(player1, List.of(new StormwingDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphTriggersOtherDragonCountersButNotSelfCounter() {
        Permanent alliedDragon = harness.addToBattlefieldAndReturn(player1, new StormwingDragon());
        Permanent stormwingDragon = castFaceDown();

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, stormwingDragon);

        assertThat(stormwingDragon.isFaceDown()).isFalse();
        assertThat(stormwingDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        Permanent lateDragon = harness.addToBattlefieldAndReturn(player1, new StormwingDragon());
        harness.passBothPriorities();

        assertThat(alliedDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(stormwingDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new StormwingDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }
}
