package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarangRiverSkeleton.class, BreakOpen.class})
class MarangRiverSkeletonTest extends BaseCardTest {

    @Test
    void megamorphsAndPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new MarangRiverSkeleton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Marang River Skeleton");
        assertThat(skeleton.isFaceDown()).isTrue();
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(skeleton));
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(skeleton.isFaceDown()).isFalse();
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void turningFaceUpWithBreakOpenDoesNotGrantMegamorphCounter() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new MarangRiverSkeleton());
        skeleton.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new BreakOpen()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, skeleton.getId());

        assertThat(skeleton.isFaceDown()).isFalse();
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Marang River Skeleton");
    }

    @Test
    void activatingRegenerationAbilityGrantsShield() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new MarangRiverSkeleton());
        skeleton.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
    }
}
