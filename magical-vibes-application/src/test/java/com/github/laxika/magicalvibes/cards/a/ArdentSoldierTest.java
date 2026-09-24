package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ArdentSoldier.class)
class ArdentSoldierTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnACounter() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Ardent Soldier");
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Ardent Soldier");
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castWithKickerRequiresAdditionalTwoMana() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void vigilanceKeepsSoldierUntappedWhenAttacking() {
        Permanent soldier = addCreatureReady(player1, new ArdentSoldier());

        declareAttackers(List.of(0));

        assertThat(soldier.isTapped()).isFalse();
    }
}
