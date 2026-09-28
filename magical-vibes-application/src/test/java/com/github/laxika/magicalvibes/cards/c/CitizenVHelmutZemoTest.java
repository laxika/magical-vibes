package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CitizenVHelmutZemo.class, DocOcksHenchmen.class, GrizzlyBears.class, AngelOfMercy.class})
class CitizenVHelmutZemoTest extends BaseCardTest {

    @Test
    @DisplayName("Puts +1/+1 counters on each Villain you control when you gain life")
    void putsCountersOnControlledVillains() {
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new CitizenVHelmutZemo());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        Permanent nonVillain = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVillain = harness.addToBattlefieldAndReturn(player2, new DocOcksHenchmen());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
