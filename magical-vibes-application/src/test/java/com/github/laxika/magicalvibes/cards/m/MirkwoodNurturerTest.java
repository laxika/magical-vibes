package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirkwoodNurturer.class, Forest.class, GrizzlyBears.class})
class MirkwoodNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another permanent you control and puts a +1/+1 counter on Mirkwood Nurturer")
    void etbReturnsAnotherPermanentAndPutsCounterOnNurturer() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castMirkwoodNurturer(List.of(forest.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
        Permanent nurturer = findPermanent(player1, "Mirkwood Nurturer");
        assertThat(nurturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can choose no permanent and does not put a counter on Mirkwood Nurturer")
    void etbCanChooseNoPermanent() {
        castMirkwoodNurturer(List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent nurturer = findPermanent(player1, "Mirkwood Nurturer");
        assertThat(nurturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMirkwoodNurturer(List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent you control");
    }

    private void castMirkwoodNurturer(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new MirkwoodNurturer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
