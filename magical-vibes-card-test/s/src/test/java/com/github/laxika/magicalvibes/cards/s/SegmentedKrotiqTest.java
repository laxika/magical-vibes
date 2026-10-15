package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SegmentedKrotiq.class, BreakOpen.class})
class SegmentedKrotiqTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new SegmentedKrotiq()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent krotiq = findPermanent(player1, "Segmented Krotiq");
        assertThat(krotiq.isFaceDown()).isTrue();
        assertThat(krotiq.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(krotiq));

        assertThat(krotiq.isFaceDown()).isFalse();
        assertThat(krotiq.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotGiveAMegamorphCounter() {
        harness.castFromHand(player1, new SegmentedKrotiq(), "{5}{G}");
        harness.passBothPriorities();

        Permanent krotiq = findPermanent(player1, "Segmented Krotiq");
        assertThat(krotiq.isFaceDown()).isFalse();
        assertThat(krotiq.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void turningFaceUpWithBreakOpenDoesNotGiveAMegamorphCounter() {
        Permanent krotiq = harness.addToBattlefieldAndReturn(player2, new SegmentedKrotiq());
        krotiq.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new BreakOpen()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, krotiq.getId());

        assertThat(krotiq.isFaceDown()).isFalse();
        assertThat(krotiq.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void turningManifestedCreatureFaceUpForItsManaCostDoesNotGiveAMegamorphCounter() {
        Permanent krotiq = harness.addToBattlefieldAndReturn(player1, new SegmentedKrotiq());
        krotiq.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        krotiq.setManifested(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.turnFaceUp(player1, 0);

        assertThat(krotiq.isFaceDown()).isFalse();
        assertThat(krotiq.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotPayMegamorphWithOnlyTheNormalManaCost() {
        Permanent krotiq = harness.addToBattlefieldAndReturn(player1, new SegmentedKrotiq());
        krotiq.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(krotiq.isFaceDown()).isTrue();
        assertThat(krotiq.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
