package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChaosSpewerTest extends BaseCardTest {

    @Test
    void payingEnterTriggerCostDoesNotBlight() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChaosSpewer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(otherCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        Permanent chaosSpewer = findPermanent(player1, "Chaos Spewer");
        assertThat(chaosSpewer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void decliningEnterTriggerBlightsChosenCreature() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChaosSpewer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, otherCreature.getId());

        assertThat(otherCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
