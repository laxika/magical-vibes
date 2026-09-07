package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MountedDreadknight.class)
class MountedDreadknightTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when an opponent lost life this turn")
    void entersWithCounterAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without a +1/+1 counter when no opponent lost life this turn")
    void entersWithoutCounterWhenNoOpponentLostLife() {
        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not get a +1/+1 counter when only its controller lost life this turn")
    void ignoresControllerLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castMountedDreadknight() {
        harness.setHand(player1, List.of(new MountedDreadknight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent findDreadknight() {
        return findPermanent(player1, "Mounted Dreadknight");
    }
}
