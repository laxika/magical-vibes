package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ErtaisTrickery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianScuta.class, ErtaisTrickery.class})
class PhyrexianScutaTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCounters() {
        harness.setHand(player1, List.of(new PhyrexianScuta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findScuta().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void castWithKickerPaysLifeAndEntersWithTwoCounters() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PhyrexianScuta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findScuta().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void castWithKickerFailsWithoutEnoughLife() {
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new PhyrexianScuta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
    }

    @Test
    void castWithoutKickerIsAllowedWhenLifeCannotPayKicker() {
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new PhyrexianScuta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findScuta().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 2);
    }

    @Test
    void kickedCreatureEntersWithCountersAtOneRemainingLifeWithoutTriggering() {
        harness.setLife(player1, 4);
        harness.setHand(player1, List.of(new PhyrexianScuta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castKickedCreature(player1, 0);

        harness.assertLife(player1, 1);
        harness.assertNotOnBattlefield(player1, "Phyrexian Scuta");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findScuta().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 1);
    }

    @Test
    void counteringKickedCreatureDoesNotRefundLifePayment() {
        PhyrexianScuta scuta = new PhyrexianScuta();
        harness.setHand(player1, List.of(scuta));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);

        harness.assertLife(player1, 17);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, scuta.getId());

        harness.assertInGraveyard(player1, "Phyrexian Scuta");
        harness.assertNotOnBattlefield(player1, "Phyrexian Scuta");
        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findScuta() {
        return findPermanent(player1, "Phyrexian Scuta");
    }
}
