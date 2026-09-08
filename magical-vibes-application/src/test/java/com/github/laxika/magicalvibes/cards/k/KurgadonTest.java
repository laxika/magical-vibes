package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kurgadon.class, ColossalDreadmaw.class, GrizzlyBears.class, JacesIngenuity.class})
class KurgadonTest extends BaseCardTest {

    @Test
    void putsThreeCountersOnItselfWhenControllerCastsCreatureWithManaValueSix() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForCreatureWithManaValueLessThanSix() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForNoncreatureSpell() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForOpponentCreatureSpell() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ColossalDreadmaw()));
        harness.addMana(player2, ManaColor.GREEN, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
