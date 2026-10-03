package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TimidShieldbearer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosSpewer.class, TimidShieldbearer.class})
class ChaosSpewerTest extends BaseCardTest {

    @Test
    void payingEnterTriggerCostDoesNotBlight() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
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
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
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

    @Test
    void decliningWithOnlyChaosSpewerBlightsItself() {
        harness.setHand(player1, List.of(new ChaosSpewer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Chaos Spewer").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(2);
        harness.assertOnBattlefield(player1, "Chaos Spewer");
    }

    @Test
    void unableToPayStillBlightsAndDoesNotAffectOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ChaosSpewer());
        harness.setHand(player1, List.of(new ChaosSpewer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Chaos Spewer").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
