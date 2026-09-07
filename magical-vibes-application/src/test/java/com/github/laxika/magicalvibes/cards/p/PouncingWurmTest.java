package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PouncingWurm.class)
class PouncingWurmTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrHaste() {
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pouncingWurm = findPermanent(player1, "Pouncing Wurm");
        assertThat(pouncingWurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, pouncingWurm, Keyword.HASTE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithThreeCountersAndHaste() {
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pouncingWurm = findPermanent(player1, "Pouncing Wurm");
        assertThat(pouncingWurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, pouncingWurm, Keyword.HASTE)).isTrue();
    }

    @Test
    void castWithKickerWithoutEnoughManaThrowsException() {
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
