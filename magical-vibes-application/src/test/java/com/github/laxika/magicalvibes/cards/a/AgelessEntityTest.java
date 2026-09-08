package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgelessEntityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts as many +1/+1 counters on itself as life gained")
    void putsCountersEqualToLifeGained() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(entity.getEffectivePower()).isEqualTo(7);
        assertThat(entity.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not trigger when only an opponent gains life")
    void doesNotTriggerForOpponentLifeGain() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
