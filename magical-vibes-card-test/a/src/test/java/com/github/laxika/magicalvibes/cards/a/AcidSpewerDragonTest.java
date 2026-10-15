package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcidSpewerDragon.class, ColossodonYearling.class})
class AcidSpewerDragonTest extends BaseCardTest {

    @Test
    void megamorphCountersOtherDragonsYouControl() {
        Permanent otherDragon = harness.addToBattlefieldAndReturn(player1, new AcidSpewerDragon());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new AcidSpewerDragon());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent acidSpewerDragon = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(acidSpewerDragon));
        harness.passBothPriorities();

        assertThat(acidSpewerDragon.isFaceDown()).isFalse();
        assertThat(acidSpewerDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void megamorphCounterIsPlacedBeforeDragonTriggerResolves() {
        Permanent otherDragon = harness.addToBattlefieldAndReturn(player1, new AcidSpewerDragon());
        Permanent dragon = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragon));

        assertThat(dragon.isFaceDown()).isFalse();
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphOnlyCountersOtherDragons() {
        Permanent otherDragon = harness.addToBattlefieldAndReturn(player1, new AcidSpewerDragon());
        Permanent dragon = castFaceDown();

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, dragon);
        harness.passBothPriorities();

        assertThat(dragon.isFaceDown()).isFalse();
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void dragonTriggerIncludesDragonsThatEnterBeforeResolution() {
        Permanent dragon = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragon));
        Permanent newDragon = harness.enterBattlefieldAndReturn(player1, new AcidSpewerDragon());
        harness.passBothPriorities();

        assertThat(newDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new AcidSpewerDragon()));
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
