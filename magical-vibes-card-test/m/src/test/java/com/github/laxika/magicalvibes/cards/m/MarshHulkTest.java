package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MarshHulk.class)
class MarshHulkTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new MarshHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent marshHulk = findPermanent(player1, "Marsh Hulk");
        assertThat(marshHulk.isFaceDown()).isTrue();
        assertThat(marshHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(marshHulk));

        assertThat(marshHulk.isFaceDown()).isFalse();
        assertThat(marshHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotGiveAMegamorphCounter() {
        harness.castFromHand(player1, new MarshHulk(), "{4}{B}{B}");
        harness.passBothPriorities();

        Permanent marshHulk = findPermanent(player1, "Marsh Hulk");
        assertThat(marshHulk.isFaceDown()).isFalse();
        assertThat(marshHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void insufficientMegamorphManaLeavesItFaceDownWithoutACounter() {
        harness.setHand(player1, List.of(new MarshHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent marshHulk = findPermanent(player1, "Marsh Hulk");
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(marshHulk)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marshHulk.isFaceDown()).isTrue();
        assertThat(marshHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manifestedHulkTurnedFaceUpForItsManaCostDoesNotGetAMegamorphCounter() {
        Permanent marshHulk = harness.addToBattlefieldAndReturn(player1, new MarshHulk());
        marshHulk.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        marshHulk.setManifested(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(marshHulk));

        assertThat(marshHulk.isFaceDown()).isFalse();
        assertThat(marshHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
